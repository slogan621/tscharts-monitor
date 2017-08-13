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
import android.os.Looper;

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
    private final int m_columnsPerPage = 5;
    private final int m_maxColumnSize = 6;
    private String m_lang = "en_US";
    private static JSONObject m_queueStatusJSON;
    private static ArrayList<Integer> m_columnsPerQueue = new ArrayList<Integer>();
    private static ArrayList<Integer> m_pageColumnCount = new ArrayList<Integer>();
    private static ArrayList<Integer> m_firstQueueThisPage = new ArrayList<Integer>();
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

    public int getFirstQueueThisPage(int idx) {
        int ret = 0;
        if (idx >= 0 && idx < m_firstQueueThisPage.size()) {
            ret = m_firstQueueThisPage.get(idx);
        }
        return ret;
    }

    public int getColumnsPerPage() {
        return m_columnsPerPage;
    }

    public int getMaxColumnSize() {
        return m_maxColumnSize;
    }

    public int getPageColumnCount(int page) {
        int ret = 0;
        if (page >= 0 && page < m_pageColumnCount.size()) {
            ret = m_pageColumnCount.get(page);
        }
        return ret;
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
        if (ret > m_maxColumnSize) {
            ret = m_maxColumnSize;
        }
        return ret;
    }

    /* total number of pages needed to display all queues */

    public int getPageCount() {
        int count = 0;
        int totalThisPage = 0;
        int firstQueueThisPage = 0;

        m_columnsPerQueue = new ArrayList<Integer>();
        m_pageColumnCount = new ArrayList<Integer>();
        m_firstQueueThisPage = new ArrayList<Integer>();
        int maxColumnSize = getMaxColumnSize();
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            for (int i = 0; i < r.length(); i++) {
                int queueCount;
                JSONObject o = r.getJSONObject(i);
                JSONArray entries = o.getJSONArray("entries");
                int n = entries.length();
                queueCount = n / maxColumnSize;
                if (queueCount == 0) {
                    queueCount = 1;
                }
                else if (n % maxColumnSize > 0) {
                    queueCount += 1;
                }
                m_columnsPerQueue.add(queueCount);
                if (i == r.length() - 1 || queueCount + totalThisPage > getColumnsPerPage()) {
                    count++;
                    if (i == r.length() - 1) {
                        totalThisPage += queueCount;
                    }
                    m_pageColumnCount.add(totalThisPage);
                    m_firstQueueThisPage.add(firstQueueThisPage);
                    firstQueueThisPage = i;
                    totalThisPage = queueCount;    // start counting for next page
                } else {
                    totalThisPage += queueCount;
                }
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

    public ArrayList<String> getLabels(int offset, int count) {
        ArrayList<String> labels = new ArrayList<String>();
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            boolean en_US = false;
            if (getLanguage().equals("en_US")) {
                en_US = true;
            }
            int queue = offset;
            while (count > 0)
            {
                if (queue < r.length()) {
                    try {
                        JSONObject o = r.getJSONObject(queue);
                        if (en_US) {
                            labels.add(o.getString("name"));
                        } else {
                            labels.add(o.getString("name_es"));
                        }
                        count--;
                    } catch (org.json.JSONException e) {
                        labels.add("");
                        count--;
                    }

                    int columnsInQueue = m_columnsPerQueue.get(queue);
                    int i = 1;
                    while (i < columnsInQueue) {
                        labels.add("");
                        count--;
                        i++;
                    }
                } else {
                    labels.add("");
                    count--;
                }
                queue++;
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

        if (o == null && Looper.myLooper() != Looper.getMainLooper()) {
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
        if (o == null) {
            return o;
        }
        return o;
    }

    public JSONObject getPatientData(final int id) {

        JSONObject o = null;

        if (m_patientData != null) {
            o = m_patientData.get(id);
        }
        if (o == null && Looper.myLooper() != Looper.getMainLooper()) {
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
        if (o == null) {
            return o;
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
                    JSONObject c = m_clinicStationData.get(o.getInt("clinicstation"));
                    if (c != null) {
                        if (c.getBoolean("active") == true) {
                            int activepatient = c.getInt("activepatient");
                            JSONObject p = getPatientData(activepatient);
                        }
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
    public ArrayList<QueueHeader> getStationHeaders(int offset, int count) {
        ArrayList<QueueHeader> queueHeader = new ArrayList<QueueHeader>();
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            int i = offset;
            while (i < offset + getColumnsPerPage() && count > 0) {
                try {
                    JSONObject o = r.getJSONObject(i);
                    String avgServiceTime = o.getString("avgservicetime");
                    QueueHeader rowHeader = new QueueHeader();
                    Date d;

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));

                    int csid = o.getInt("clinicstation");
                    JSONObject clinicStation = getClinicStationData(csid);

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
                            int activePatient = clinicStation.getInt("activepatient");
                            rowHeader.setActivePatient(activePatient);
                        } else {
                            rowHeader.setState(QueueHeader.State.WAITING);
                        }
                    } catch (JSONException e) {
                        continue;
                    }

                    queueHeader.add(rowHeader);
                    count--;

                    if (m_columnsPerQueue.get(i) > 1) {
                        for (int j = 0; j < m_columnsPerQueue.get(i) - 1; j++) {
                            // XXX obviously will need to push something different but for now...
                            queueHeader.add(rowHeader);
                            count--;
                        }
                    }
                } catch (JSONException e) {
                   continue;
                }
                i++;
            }
        }
        catch (org.json.JSONException e) {
        }
        return queueHeader;
    }

    private String patientToString(int patient, JSONObject p) {
        String dob;
        String gender;
        String ret;

        try {
            gender = p.getString("gender");

            if (m_lang.equals("en_US")) {
                dob = String.format(m_ctx.getResources().getString(R.string.dob));
                if (gender.equals("Female")) {
                    gender = String.format(m_ctx.getResources().getString(R.string.female));
                } else {
                    gender = String.format(m_ctx.getResources().getString(R.string.male));
                }
            } else {
                dob = String.format(m_ctx.getResources().getString(R.string.dob_es));
                if (gender.equals("Female")) {
                    gender = String.format(m_ctx.getResources().getString(R.string.female_es));
                } else {
                    gender = String.format(m_ctx.getResources().getString(R.string.male_es));
                }
            }

            ret = String.format("%d: %s-%s, %c\n%s %s: %s\n",
                    patient,
                    p.getString("paternal_last"),
                    p.getString("maternal_last"),
                    p.getString("first").charAt(0),
                    gender,
                    dob,
                    p.getString("dob"));
        } catch (JSONException e) {
            ret = "";
        }
        return ret;
    }

    public ArrayList<String> getRow(int offset, int row, int count) {
        ArrayList<String> rowdata = new ArrayList<String>();
        int maxColumnSize = getMaxColumnSize();

        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            int i = offset;
            do {
                try {
                    JSONObject o = r.getJSONObject(i);
                    JSONArray entries = o.getJSONArray("entries");
                    int patient = entries.getJSONObject(row).getInt("patient");
                    String waitTime = entries.getJSONObject(row).getString("estwaittime");
                    JSONObject p = getPatientData(patient);
                    String patientString = patientToString(patient, p);
                    String estimated;

                    if (m_lang.equals("en_US")) {
                        estimated = String.format(m_ctx.getResources().getString(R.string.estimated_waiting_time));
                    } else {
                        estimated = String.format(m_ctx.getResources().getString(R.string.estimated_waiting_time_es));
                    }

                    patientString += String.format("%s: %s\n", estimated, waitTime);

                    rowdata.add(patientString);
                    count--;

                    if (m_columnsPerQueue.get(i) > 1) {
                        int columnOffset = maxColumnSize;
                        for (int j = 0; j < m_columnsPerQueue.get(i) - 1; j++) {
                            // XXX obviously will need to push something different but for now...
                            patient = entries.getJSONObject(row + columnOffset).getInt("patient");
                            waitTime = entries.getJSONObject(row + columnOffset).getString("estwaittime");
                            p = getPatientData(patient);
                            patientString = patientToString(patient, p);

                            if (m_lang.equals("en_US")) {
                                estimated = String.format(m_ctx.getResources().getString(R.string.estimated_waiting_time));
                            } else {
                                estimated = String.format(m_ctx.getResources().getString(R.string.estimated_waiting_time_es));
                            }

                            patientString += String.format("%s: %s\n", estimated, waitTime);

                            rowdata.add(patientString);
                            columnOffset += maxColumnSize;
                            count--;
                        }
                    }
                } catch (org.json.JSONException e) {
                    rowdata.add("");
                    count--;
                }
                i = i + 1;
            } while(count > 0);
        }
        catch (org.json.JSONException e) {
        }
        return rowdata;
    }

    public ArrayList<String> getActiveRow(int offset, int count) {
        ArrayList<String> rowdata = new ArrayList<String>();
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            int i = offset;
            while (count > 0)
            {
                try {
                    JSONObject o = r.getJSONObject(i);
                    String patientString;
                    if (o != null) {
                        int clinicstation = o.getInt("clinicstation");
                        JSONObject c = getClinicStationData(clinicstation);

                        if (c != null) {

                            if (c.getBoolean("active") == true) {

                                int activePatient = c.getInt("activepatient");
                                JSONObject p = getPatientData(activePatient);

                                if (p != null) {
                                    patientString = patientToString(activePatient, p);
                                } else {
                                    patientString = "";
                                }
                            } else {
                                patientString = "";
                            }
                        } else {
                            patientString = "";
                        }
                    } else {
                        patientString = "";
                    }

                    rowdata.add(patientString);

                }
                catch(org.json.JSONException e) {
                    rowdata.add("");
                }

                if (m_columnsPerQueue.get(i) > 1) {
                    for (int j = 1; j < m_columnsPerQueue.get(i) && count > 0; j++, count--) {
                        rowdata.add("");
                    }
                }
                i++;
                count--;
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

