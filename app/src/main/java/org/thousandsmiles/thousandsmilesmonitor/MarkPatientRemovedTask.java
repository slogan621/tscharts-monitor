package org.thousandsmiles.thousandsmilesmonitor;

/*
 * (C) Copyright Syd Logan 2019
 * (C) Copyright Thousand Smiles Foundation 2019
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;
import org.thousandsmiles.tscharts_lib.RoutingSlipEntryREST;

public class MarkPatientRemovedTask extends AsyncTask<Object, Object, Object> {
    private Activity m_activity;
    private int m_routingSlip;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    @Override
    protected String doInBackground(Object... params) {
        if (params.length > 0) {
            RowData rd = (RowData) params[0];
            m_routingSlip = rd.getRoutingSlip();
            m_activity = (Activity) params[1];
            JSONArray ret = getRoutingSlipEntriesByStates(m_routingSlip, "New,Scheduled");
            if (ret != null) {
              markRoutingSlipEntriesRemoved(ret);
            }
        }
        return "";
    }

    public class GetRoutingSlipEntriesByStatesListener implements RESTCompletionListener {

        JSONArray m_result;

        public void onFail(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg)
        {
        }

        public void onSuccess(int code, String msg, JSONObject o)
        {
        }

        public void onSuccess(int code, String msg, JSONArray a)
        {
            m_result = a;
        }
    }

    private boolean markRoutingSlipEntriesRemoved(JSONArray entries)
    {
        boolean ret = true;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final RoutingSlipEntryREST rsData = new RoutingSlipEntryREST(m_activity);

            for (int i = 0; i < entries.length(); i++)
            {
                try {
                  int entry = entries.getInt(i);
                  Object lock = rsData.markRoutingSlipStateRemoved(entry);

                    synchronized (lock) {
                        // we loop here in case of race conditions or spurious interrupts
                        while (true) {
                            try {
                                lock.wait();
                                break;
                            } catch (InterruptedException e) {
                                continue;
                            }
                        }
                    }

                    int status = rsData.getStatus();
                    if (status != 200) {
                        ret = false;
                        break;
                    }
                }
                catch (Exception e) {
                }
            }
        }

        if (ret == true) {
            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    ((StationActivity)m_activity).setRefresh();
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_patient_successfully_deleted_from_clinic), Toast.LENGTH_SHORT).show();
                }
            });

        } else {

            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_patient_unsuccessfully_deleted_from_clinic), Toast.LENGTH_SHORT).show();
                }
            });
        }
        return ret;
    }

    private JSONArray getRoutingSlipEntriesByStates(int routingSlip, String states)
    {
        JSONArray ret = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final RoutingSlipEntryREST rsData = new RoutingSlipEntryREST(m_activity);
            GetRoutingSlipEntriesByStatesListener listener = new GetRoutingSlipEntriesByStatesListener();
            rsData.addListener(listener);
            Object lock = rsData.getRoutingSlipEntriesByStates(routingSlip, states);

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }

            int status = rsData.getStatus();
            if (status == 200) {
                ret = listener.m_result;
            }
        }
        return ret;
    }

    // This is called from background thread but runs in UI
    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        // Do things like update the progress bar
    }

    // This runs in UI when background thread finishes
    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);

        // Do things like hide the progress bar or change a TextView
    }
}
