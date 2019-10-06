/*
 * (C) Copyright Syd Logan 2017-2019
 * (C) Copyright Thousand Smiles Foundation 2017-2019
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
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class SessionSingleton {
    private static SessionSingleton m_instance;
    private int m_columnsPerPage = 4;
    private int m_maxColumnsPerPage = 4;
    private int m_maxColumnSize = 5;
    private int m_curColumnSize = 5;
    private int m_height = -1;
    private int m_width = -1;
    private final int m_minPatientColumnWidth = 400;
    private int m_patientRowHeight = 300;
    private int m_patientColumnWidth = m_minPatientColumnWidth;
    private int m_headerHeight = 0;
    private float m_density;
    private String m_lang = "en_US";
    private static JSONObject m_queueStatusJSON;
    private ArrayList<MonitorPage> m_monitorPages = new ArrayList<MonitorPage>();
    private static ArrayList<Integer> m_columnsPerQueue = new ArrayList<Integer>();
    private static HashMap<Integer, String> m_stationIdToName = new HashMap<Integer, String>();
    private static HashMap<Integer, JSONObject> m_patientData = new HashMap<Integer, JSONObject>();
    private static HashMap<Integer, JSONObject> m_clinicStationData = new HashMap<Integer, JSONObject>();
    private CommonSessionSingleton m_commonSessionSingleton = null;

    public CommonSessionSingleton getCommonSessionSingleton() {
        if (m_commonSessionSingleton == null) {
            m_commonSessionSingleton = CommonSessionSingleton.getInstance();
        }
        return m_commonSessionSingleton;
    }

    public boolean isXRay(int clinicStationId)
    {
        boolean ret = false;
        JSONObject o = m_clinicStationData.get(clinicStationId);
        if (o != null) {
            try {
                int stationId = o.getInt("station");
                String name = m_stationIdToName.get(stationId);
                if (name != null && (name.equals("X-Ray") || name.equals("XRay"))) {
                    ret = true;
                }
            } catch (Exception e) {
            }
        }
        return ret;
    }

    public Context getContext() {
        return getCommonSessionSingleton().getContext();
    }

    public void setContext(Context ctx) {
        getCommonSessionSingleton().setContext(ctx);
    }

    public void setQueueStatusJSON(JSONObject obj) {
        m_queueStatusJSON = obj;
    }

    public void setLanguage(String lang) {
        m_lang = lang;
    }

    public String getLanguage() {
        return m_lang;
    }

    public void addStationData(JSONArray data) {
        int i;
        JSONObject stationdata;

        for (i = 0; i < data.length(); i++)  {
            try {
                stationdata = data.getJSONObject(i);
                m_stationIdToName.put(stationdata.getInt("id"), stationdata.getString("name"));
            } catch (JSONException e) {
                return;
            }
        }
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
                format = getContext().getResources().getString(R.string.format_status_banner);
            } else {
                format = getContext().getResources().getString(R.string.format_status_banner_es);
            }
            ret =  String.format(format, avgwait, minwait, maxwait, avgq, minq, maxq, numwaiting);
        }
        catch (org.json.JSONException e)
        {
            ret = "";
        }
        return ret;
    }

    public int getColumnsPerPage() {
        if (m_width == -1 && m_height == -1) {
            getScreenResolution(getContext());
        }
        m_columnsPerPage = m_width / getPatientColumnWidth();
        if (m_columnsPerPage > m_maxColumnsPerPage) {
            m_columnsPerPage = m_maxColumnsPerPage;
        }
        return m_columnsPerPage;
    }

    public void clearPatientData()
    {
        m_patientData.clear();
    }

    public void setPatientRowHeight(int height)
    {
        m_patientRowHeight = height;
    }

    public void setPatientColumnWidth(int width)
    {
        /*
        if (width > m_minPatientColumnWidth) {
            m_patientColumnWidth = width;
        }
        */
    }

    private int getPatientRowHeight()
    {
        return (int) (m_patientRowHeight / m_density);
    }

    private int getPatientColumnWidth()
    {
        return (int) (m_patientColumnWidth / m_density);
    }

    public void setHeaderHeight(int height)
    {
        m_headerHeight = height;
    }

    private int getHeaderHeight()
    {
        return (int) (m_headerHeight / m_density);
    }

    private void getScreenResolution(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        m_width = (int) (metrics.widthPixels / metrics.density);
        m_height = (int) (metrics.heightPixels / metrics.density);
        m_density = metrics.density;
    }

    public int getCurColumnSize() {
        if (m_width == -1 && m_height == -1) {
            getScreenResolution(getContext());
        }

        if (getPatientRowHeight() > 0) {
            m_curColumnSize = (m_height - getHeaderHeight()) / getPatientRowHeight();
        } else {
            m_curColumnSize = 1;  // avoid divide by zero
        }

        if (m_curColumnSize > m_maxColumnSize) {
            m_curColumnSize = m_maxColumnSize;
        }

        return m_curColumnSize;
    }

    public int getPageColumnCount(int page) {
        int ret = 0;
        if (page >= 0 && page < m_monitorPages.size()) {
            ret = m_monitorPages.get(page).columnCount();
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
        if (ret > m_curColumnSize) {
            ret = m_curColumnSize;
        }
        return ret;
    }

    /* total number of pages needed to display all queues */

    public int getPageCount() {
        int count = 0;
        int queueCount = 0;

        m_monitorPages.clear();
        m_columnsPerQueue = new ArrayList<Integer>();
        int curColumnSize = getCurColumnSize();
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");

            MonitorPage monPage = new MonitorPage();
            monPage.setFirstQueue(0);
            m_monitorPages.add(monPage);
            count = 1;
            int columnsPerPage = getColumnsPerPage();
            int j = 0;

            for (int i = 0; i < r.length(); i++) {
                int columnCount;

                JSONObject o = r.getJSONObject(i);
                JSONArray entries = o.getJSONArray("entries");
                int n = entries.length();
                boolean en_US = false;
                if (getLanguage().equals("en_US")) {
                    en_US = true;
                }
                String stationName;

                if (en_US == true) {
                    stationName = o.getString("name");
                } else {
                    stationName = o.getString("name_es");
                }
                columnCount = n / curColumnSize;
                if (n % curColumnSize != 0) {
                    columnCount++;
                }
                int colNumber = 0;
                int offset = 0;
                if (columnCount == 0) {
                    columnCount = 1;     // always display a column, even if no entries in the queue
                }

                m_columnsPerQueue.add(columnCount);
                while (columnCount > 0) {
                    String header;
                    PageColumn column = new PageColumn();
                    if (colNumber == 0) {
                        header = stationName;
                        column.setOverflow(false);
                    } else {
                        header = String.format("%s (%d)", stationName, colNumber + 1);
                        column.setOverflow(true);
                    }
                    colNumber++;

                    column.setQueue(i);
                    column.setHeader(header);
                    if (columnCount > 1) {
                        column.setOffset(offset);
                        offset += curColumnSize;
                    } else {
                        column.setOffset(offset);
                    }
                    j++;
                    monPage.addColumn(column);
                    columnCount--;

                    if (j == columnsPerPage && (columnCount > 0 || i < (r.length() - 1)))  {
                        monPage = new MonitorPage();
                        monPage.setFirstQueue(i + 1);
                        m_monitorPages.add(monPage);
                        j = 0;
                        count++;
                    }
                }
            }
        }
        catch (org.json.JSONException e) {
            count = 0;
        }

        return count;
    }

    public ArrayList<String> getLabels(int page, int count) {
        ArrayList<String> labels = new ArrayList<String>();

      MonitorPage p = m_monitorPages.get(page);
      for (int j = 0; j < count; j++) {
          PageColumn c = p.getColumn(j);
          labels.add(c.getHeader());
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
                m_commonSessionSingleton.isNewPatient(id);  // go preload the new patient map
                m_commonSessionSingleton.hasCurrentXRay(id, 365);
            }
        }
        if (o == null) {
            return o;
        }
        return o;
    }

    public boolean updatePatientList() {
        boolean ret = true;
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            for (int i = 0; i < r.length(); i++) {
                try {
                    JSONObject o = r.getJSONObject(i);
                    JSONArray entries = o.getJSONArray("entries");
                    for (int j = 0; j < entries.length(); j++) {
                        int patient = entries.getJSONObject(j).getInt("patient");
                        JSONObject p = getPatientData(patient);
                        if (p == null) {
                            ret = false;
                            break;
                        }
                    }
                    JSONObject c = m_clinicStationData.get(o.getInt("clinicstation"));
                    if (c != null) {
                        if (c.getBoolean("active") == true) {
                            int activepatient = c.getInt("activepatient");
                            JSONObject p = getPatientData(activepatient);
                            if (p == null) {
                                ret = false;
                            }
                        }
                     }
                } catch (JSONException e) {
                    ret = false;
                }

            }
        } catch (JSONException e) {
            ret = false;
        }
        return ret;
    }

    public boolean updateClinicStationList() {
        boolean ret = true;
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            clearClinicStationData();
            for (int i = 0; i < r.length(); i++) {
                try {
                    JSONObject o = r.getJSONObject(i);
                    int clinicstation = o.getInt("clinicstation");
                    if (getClinicStationData(clinicstation) == null) {
                        ret = false;
                    }
                } catch (JSONException e) {
                }

            }
        } catch (JSONException e) {
            ret = false;
        }
        return ret;
    }

    //{"name":"Dental1","level":1,"away":true,"awaytime":30,"clinic":1,"station":1,"active":false,"willreturn":"2017-07-28T04:49:14","id":1}
    public ArrayList<QueueHeader> getStationHeaders(int page, int count) {
        ArrayList<QueueHeader> queueHeader = new ArrayList<QueueHeader>();
        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");

            MonitorPage p = m_monitorPages.get(page);
            for (int i = 0; i < p.columnCount(); i++) {
                QueueHeader rowHeader = new QueueHeader();
                PageColumn column = p.getColumn(i);
                int queue = column.getQueue();
                if (column.getOverflow() == true) {
                    rowHeader.setStub(true);
                } else {
                    try {
                        JSONObject o = r.getJSONObject(queue);
                        String avgServiceTime = o.getString("avgservicetime");
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
                            } else if (clinicStation.getBoolean("finished") == true) {
                                rowHeader.setState(QueueHeader.State.FINISHED);
                            } else {
                                rowHeader.setState(QueueHeader.State.WAITING);
                            }
                        } catch (JSONException e) {
                            continue;
                        }
                    } catch (JSONException e) {
                    }
                }
                queueHeader.add(rowHeader);
            }
        } catch(JSONException e){
            e.printStackTrace();
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
                dob = String.format(getContext().getResources().getString(R.string.dob));
                if (gender.equals("Female")) {
                    gender = String.format(getContext().getResources().getString(R.string.female));
                } else {
                    gender = String.format(getContext().getResources().getString(R.string.male));
                }
            } else {
                dob = String.format(getContext().getResources().getString(R.string.dob_es));
                if (gender.equals("Female")) {
                    gender = String.format(getContext().getResources().getString(R.string.female_es));
                } else {
                    gender = String.format(getContext().getResources().getString(R.string.male_es));
                }
            }

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            Boolean val = sharedPref.getBoolean("showDOB", true);
            if (val == true) {
                ret = String.format("%05d %s %s\n%.10s-%.10s, %.10s\n",
                        patient,
                        p.getString("dob"),
                        gender,
                        p.getString("paternal_last"),
                        p.getString("maternal_last"),
                        p.getString("first"));
            } else {
                ret = String.format("%05d %s\n%.10s-%.10s, %.10s\n",
                        patient,
                        gender,
                        p.getString("paternal_last"),
                        p.getString("maternal_last"),
                        p.getString("first"));
            }
        } catch (JSONException e) {
            ret = "";
        }
        return ret;
    }

    public String patientNameToString(JSONObject patientData)
    {
        String ret = "";
        try {
            ret = String.format("%.10s-%.10s, %.10s\n",
                    patientData.getString("paternal_last"),
                    patientData.getString("maternal_last"),
                    patientData.getString("first"));
        } catch (Exception e) {
        }
        return ret;
    }

    public ArrayList<RowData> getRow(int page, int row, int count) {
        ArrayList<RowData> rowdata = new ArrayList<RowData>();

        try {
            JSONArray r = m_queueStatusJSON.getJSONArray("queues");
            MonitorPage mp = m_monitorPages.get(page);
            int aCount = count;
            int i = 0;

            while (aCount > 0) {
                PageColumn pc = mp.getColumn(i);
                int offset = pc.getOffset();
                RowData rd = new RowData();

                try {
                    JSONObject o = r.getJSONObject(pc.getQueue());
                    rd.setQueue(pc.getQueue());
                    JSONObject entry = o.getJSONArray("entries").getJSONObject(row + offset);
                    int patient = entry.getInt("patient");
                    JSONObject patientData = getPatientData(patient);
                    rd.setPatientName(patientNameToString(patientData));
                    rd.setPatientid(patient);

                    int clinicstation = o.getInt("clinicstation");
                    JSONObject c = getClinicStationData(clinicstation);
                    if (c != null) {
                        rd.setClinicStationName(c.getString("name"));
                    }
                    rd.setClinicstation(clinicstation);
                    rd.setIsXray(isXRay(clinicstation));
                    rd.setRoutingSlipEntry(entry.getInt("routingslipentry"));
                    rd.setRoutingSlip(entry.getInt("routingslip"));
                    rd.setIsNewPatient(m_commonSessionSingleton.isNewPatient(patient));
                    rd.setIsCurrentXray(m_commonSessionSingleton.hasCurrentXRay(patient, 365));
                    String waitTime = entry.getString("waittime");
                    JSONObject p = getPatientData(patient);
                    String patientString = "";
                    if (p != null) {
                        patientString = patientToString(patient, p);
                        String gender = p.getString("gender");
                        if (gender != null) {
                            if(gender.equals("Female")) {
                                rd.setIsMale(false);
                            } else {
                                rd.setIsMale(true);
                            }
                        }
                    }
                    String waiting;

                    if (m_lang.equals("en_US")) {
                        waiting = String.format(getContext().getResources().getString(R.string.waiting_time));
                    } else {
                        waiting = String.format(getContext().getResources().getString(R.string.waiting_time_es));
                    }

                    patientString += String.format("%s: %s\n", waiting, waitTime);
                    rd.setRowdata(patientString);
                    if (offset == 0 && row == 0) {
                        rd.setWaitingItem(true);
                    } else {
                        rd.setWaitingItem(false);
                    }
                    rowdata.add(rd);

                } catch(JSONException e) {
                    rd.setRowdata("");
                    rd.setWaitingItem(false);
                    rowdata.add(rd);
                }
                aCount--;
                i++;
            }
        } catch(JSONException e) {
        }

        return rowdata;
    }

    public ArrayList<RowData> getActiveRow(int page, int count) {
        ArrayList<RowData> rowdata = new ArrayList<RowData>();

        MonitorPage mp = m_monitorPages.get(page);
        JSONArray r;
        try {
            r = m_queueStatusJSON.getJSONArray("queues");
        } catch (JSONException e) {
            return rowdata;
        }

        for (int i = 0; i < mp.columnCount(); i++) {
            PageColumn pc = mp.getColumn(i);
            RowData rd = new RowData();
            rd.setWaitingItem(false);

            if (pc.getOverflow() == true) {
                rd.setRowdata("");
                rowdata.add(rd);
            } else {
                int queue = pc.getQueue();
                rd.setQueue(queue);
                try {
                    JSONObject o = r.getJSONObject(queue);
                    String patientString;
                    if (o != null) {
                        int clinicstation = o.getInt("clinicstation");
                        JSONObject c = getClinicStationData(clinicstation);

                        rd.setClinicstation(clinicstation);

                        if (c != null) {
                            rd.setClinicStationName(c.getString("name"));
                            if (c.getBoolean("active") == true) {

                                int activePatient = c.getInt("activepatient");
                                rd.setPatientid(activePatient);
                                JSONObject p = getPatientData(activePatient);

                                if (p != null) {
                                    rd.setPatientName(patientNameToString(p));
                                    String gender = p.getString("gender");
                                    if (gender != null) {
                                        if(gender.equals("Female")) {
                                            rd.setIsMale(false);
                                        } else {
                                            rd.setIsMale(true);
                                        }
                                    }
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
                    rd.setRowdata(patientString);
                    rowdata.add(rd);
                } catch (JSONException e) {
                    rowdata.clear();
                    return rowdata;
                }
            }
        }
        return rowdata;
    }

    public int getClinicId() {
        return m_commonSessionSingleton.getClinicId();
    }

    public static SessionSingleton getInstance() {
        if (m_instance == null) {
            m_instance = new SessionSingleton();
        }
        return m_instance;
    }
}

