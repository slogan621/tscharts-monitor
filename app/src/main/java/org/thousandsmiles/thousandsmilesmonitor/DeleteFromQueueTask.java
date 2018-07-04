package org.thousandsmiles.thousandsmilesmonitor;

/*
 * (C) Copyright Syd Logan 2018
 * (C) Copyright Thousand Smiles Foundation 2018
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
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.RoutingSlipEntryREST;

public class DeleteFromQueueTask extends AsyncTask<Object, Object, Object> {

    private RowData m_params;
    private Activity m_activity;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    @Override
    protected String doInBackground(Object... params) {
        if (params.length > 0) {
            m_params = (RowData) params[0];
            m_activity = (Activity) params[1];
            goAway();
        }
        return "";
    }

    private void goAway()
    {
        int status;
        Object lock;

        final RoutingSlipEntryREST rse = new RoutingSlipEntryREST(m_sess.getContext());
        lock = rse.deleteRoutingSlipEntry(m_params.getRoutineslipentry());

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
        status = rse.getStatus();
        if (status == 200) {

            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_patient_successfully_removed), Toast.LENGTH_SHORT).show();
                }
            });

        } else {

            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_patient_unsuccessfully_removed), Toast.LENGTH_SHORT).show();
                }
            });
        }
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