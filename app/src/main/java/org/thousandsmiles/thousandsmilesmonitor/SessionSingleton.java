/*
 * (C) Copyright Syd Logan 2017
 * (C) Copyright Thousand Smiles Foundation 2017
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

package org.thousandsmiles.thousandsmilesmonitor;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class SessionSingleton {
    private static SessionSingleton m_instance;
    private static String m_token = "";
    private static int m_clinicId;
    private static Context m_ctx;
    private String m_lang = "en_US";
    private static JSONObject m_queueStatusJSON;
    private static HashMap<Integer, JSONObject> m_patientData = new HashMap<Integer, JSONObject>();
    private static HashMap<Integer, JSONObject> m_clinicStationData = new HashMap<Integer, JSONObject>();

    public void setToken(String token) {
        m_token = String.format("Token %s", token);
    }

    public void setQueueStatusJSON(JSONObject obj)
    {
        m_queueStatusJSON = obj;
    }

    public void setLanguage(String lang) {
        m_lang = lang;
    }

    public String getLanguage() {
        return m_lang;
    }

    public String getOverallStatus()
    {
        // "avgwait":"00:01:37","maxwait":"00:02:24","maxq":2,"numwaiting":8,"minwait":"00:00:31","minq":1,"avgq":1}
        String ret;
        JSONObject status;
        String avgwait;
        String minwait;
        String maxwait;
        int avgq;
        int minq;
        int maxq;
        int numwaiting;

        try {
            status = m_queueStatusJSON.getJSONObject("status");
            avgwait = status.getString("avgwait");
            minwait = status.getString("minwait");
            maxwait = status.getString("maxwait");
            avgq = status.getInt("avgq");
            minq = status.getInt("minq");
            maxq = status.getInt("maxq");
            numwaiting = status.getInt("numwaiting");
            String format;
            if (m_lang.equals("en_US")) {
                format = m_ctx.getResources().getString(R.string.format_status_banner);
            } else {
                format = m_ctx.getResources().getString(R.string.format_status_banner_es);
            }
            ret =  String.format(format, avgwait, minwait, maxwait, avgq, minq, maxq, numwaiting);
        }
        catch (org.json.JSONException e)
        {
            ret = "";
        }
        return ret;
    }

    public int getPageSize() {
        return 5;
    }

    public int getNumberOfRows()
    {
        int ret = 0;
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");

            for (int i = 0; i < r.length(); i++) {
                JSONObject o = r.getJSONObject(i);
                int len = o.getJSONArray("entries").length();
                if (len > ret) {
                    ret = len;
                }
            }
        }
        catch (org.json.JSONException e) {
            ret = 0;
        }
        return ret;
    }

    public int getPageCount() {
        int count;

        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            count = r.length() / getPageSize();
            if (r.length() % getPageSize() > 0) {
                count += 1;
            }
        }
        catch (org.json.JSONException e) {
            count = 0;
        }
        return count;
    }

    public String getToken() {
        return m_token;
    }

    public void setClinicId(int id) {
        m_clinicId = id;
    }

    public ArrayList<String> getLabels(int offset) {
        ArrayList<String> labels = new ArrayList<String>();
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            for (int i = offset; i < offset + 5; i++)
            {
                try {
                    JSONObject o = r.getJSONObject(i);
                    if (getLanguage().equals("en_US")) {
                        labels.add(o.getString("name"));
                    } else {
                        labels.add(o.getString("name_es"));
                    }
                }
                catch(org.json.JSONException e) {
                    labels.add("");
                }
            }
        }
        catch (org.json.JSONException e) {
        }
        return labels;
    }

    public void addPatientData(JSONObject data) {
        int id;

        try {
            id = data.getInt("id");
        } catch (JSONException e) {
            return;
        }
        m_patientData.put(id, data);
    }

    public void addClinicStationData(JSONObject data) {
        int id;

        try {
            id = data.getInt("id");
        } catch (JSONException e) {
            return;
        }
        m_clinicStationData.put(id, data);
    }

    public void clearClinicStationData() {
        m_clinicStationData.clear();
    }

    public JSONObject getClinicStationData(final int id) {

        JSONObject o = null;

        if (m_clinicStationData != null) {
            o = m_clinicStationData.get(id);
        }
        if (o == null) {
            final ClinicStationREST clinicStationData = new ClinicStationREST(getContext());
            Object lock = clinicStationData.getClinicStationData(id);

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

            int status = clinicStationData.getStatus();
            if (status == 200) {
                o = m_clinicStationData.get(id);
            }
        }
        return o;
    }

    public JSONObject getPatientData(final int id) {

        JSONObject o = null;

        if (m_patientData != null) {
            o = m_patientData.get(id);
        }
        if (o == null) {
            final PatientREST patientData = new PatientREST(getContext());
            Object lock = patientData.getPatientData(id);

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

            int status = patientData.getStatus();
            if (status == 200) {
                o = m_patientData.get(id);
            }
        }
        return o;
    }

    public void updatePatientList() {
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            for (int i = 0; i < r.length(); i++) {
                try {
                    JSONObject o = r.getJSONObject(i);
                    JSONArray entries = o.getJSONArray("entries");
                    for (int j = 0; j < entries.length(); j++) {
                        int patient = entries.getJSONObject(j).getInt("patient");
                        JSONObject p = getPatientData(patient);
                    }
                } catch (JSONException e) {

                }

            }
        } catch (JSONException e) {
        }
    }

    public void updateClinicStationList() {
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            clearClinicStationData();
            for (int i = 0; i < r.length(); i++) {
                try {
                    JSONObject o = r.getJSONObject(i);
                    int clinicstation = o.getInt("clinicstation");
                    getClinicStationData(clinicstation);
                } catch (JSONException e) {
                }

            }
        } catch (JSONException e) {
        }
    }
    //{"name":"Dental1","level":1,"away":true,"awaytime":30,"clinic":1,"station":1,"active":false,"willreturn":"2017-07-28T04:49:14","id":1}
    public ArrayList<QueueHeader> getStationHeaders(int offset) {
        ArrayList<QueueHeader> queueHeader = new ArrayList<QueueHeader>();
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            for (int i = offset; i < offset + 5; i++) {
                try {
                    JSONObject o = r.getJSONObject(i);
                    String avgServiceTime = o.getString("avgservicetime");
                    QueueHeader rowHeader = new QueueHeader();
                    Date d;

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));

                    int csid = o.getInt("clinicstation");
                    JSONObject clinicStation = SessionSingleton.getInstance().getClinicStationData(csid);

                    rowHeader.setServiceTime(avgServiceTime);

                    String willret = clinicStation.getString("willreturn");

                    try {
                        d = df.parse(willret);
                        SimpleDateFormat dflocal = new SimpleDateFormat("hh:mm:ss a");
                        dflocal.setTimeZone(TimeZone.getDefault());
                        willret = dflocal.format(d);
                    } catch (ParseException e) {
                        continue;
                    }

                    try {
                        if (clinicStation.getBoolean("away") == true) {
                            rowHeader.setState(QueueHeader.State.AWAY);
                            rowHeader.setWillReturnTime(willret);
                        } else if (clinicStation.getBoolean("active") == true) {
                            rowHeader.setState(QueueHeader.State.ACTIVE);
                        } else {
                            rowHeader.setState(QueueHeader.State.WAITING);
                        }
                    } catch (JSONException e) {
                        continue;
                    }

                    queueHeader.add(rowHeader);
                } catch (JSONException e) {
                   continue;
                }
            }
        }
        catch (org.json.JSONException e) {
        }
        return queueHeader;
    }

    public ArrayList<String> getRow(int offset, int row) {
        ArrayList<String> rowdata = new ArrayList<String>();
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            for (int i = offset; i < offset + 5; i++)
            {
                try {
                    JSONObject o = r.getJSONObject(i);
                    JSONArray entries = o.getJSONArray("entries");
                    int patient = entries.getJSONObject(row).getInt("patient");
                    String waitTime = entries.getJSONObject(row).getString("estwaittime");
                    JSONObject p = getPatientData(patient);
                    String estimated;
                    String dob;
                    String gender = p.getString("gender");

                    if (m_lang.equals("en_US")) {
                        estimated = String.format(m_ctx.getResources().getString(R.string.estimated_waiting_time));
                        dob = String.format(m_ctx.getResources().getString(R.string.dob));
                        if (gender.equals("Female")) {
                            gender = String.format(m_ctx.getResources().getString(R.string.female));
                        } else {
                            gender = String.format(m_ctx.getResources().getString(R.string.male));
                        }
                    } else {
                        estimated = String.format(m_ctx.getResources().getString(R.string.estimated_waiting_time_es));
                        dob = String.format(m_ctx.getResources().getString(R.string.dob_es));
                        if (gender.equals("Female")) {
                            gender = String.format(m_ctx.getResources().getString(R.string.female_es));
                        } else {
                            gender = String.format(m_ctx.getResources().getString(R.string.male_es));
                        }
                    }

                    rowdata.add(String.format("%d: %s-%s, %c\n%s %s: %s\n%s: %s",
                                                                  patient,
                                                                  p.getString("paternal_last"),
                                                                  p.getString("maternal_last"),
                                                                  p.getString("first").charAt(0),
                                                                  gender,
                                                                  dob,
                                                                  p.getString("dob"),
                                                                  estimated,
                                                                  waitTime));
                }
                catch(org.json.JSONException e) {
                    rowdata.add("");
                }
            }
        }
        catch (org.json.JSONException e) {
        }
        return rowdata;
    }

    public int getClinicId() {
        return m_clinicId;
    }

    public static SessionSingleton getInstance() {
        if (m_instance == null) {
            m_instance = new SessionSingleton();
        }
        return m_instance;
    }

    public void setContext(Context ctx) {
        m_ctx = ctx;
    }

    public Context getContext() {
        return m_ctx;
    }
}

