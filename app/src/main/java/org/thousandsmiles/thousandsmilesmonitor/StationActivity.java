/*
 * (C) Copyright Syd Logan 2017-2018
 * (C) Copyright Thousand Smiles Foundation 2017-2018
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

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.thousandsmiles.tscharts_lib.ClinicREST;
import org.thousandsmiles.tscharts_lib.HideyHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.view.View.VISIBLE;
import static java.lang.Math.abs;

public class StationActivity extends AppCompatActivity {
    private static final int TIME_OUT = 15000;
    GetAndDisplayTask m_task = null;
    Context m_context;
    boolean m_swiped = false;
    boolean m_refresh = false;
    private GestureDetectorCompat m_detector;

    public void setRefresh()
    {
        m_refresh = true;
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            //Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
            /*
             * XXX in some cases, veloxityX is neg and in some, pos, could be due to
             *  tablet orientation. Needs to be investigated. But for now, any swipe that
             * is along the x axis will trigger the swipe
             */
            if (abs((int)velocityX) > abs((int)velocityY)) {
                m_swiped = true;
            }
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.m_detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class GetAndDisplayTask extends AsyncTask<String, Integer, String> {

        private Context m_context;
        private SessionSingleton m_sess = SessionSingleton.getInstance();

        private void displayOverallStatus()
        {
            StationActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    TextView text = (TextView) findViewById(R.id.clinicdatetime);
                    text.setText(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date()));
                    text = (TextView) findViewById(R.id.clinicstatus);
                    String status = m_sess.getOverallStatus();
                    text.setText(status);
                }
            });
        }

        private void cleanStationTable(TableLayout table) {

            if (table != null) {
                int childCount = table.getChildCount();

                // Remove all rows except the headers and other static rows near the top

                boolean done = false;
                TableRow row = null;
                int index = childCount;
                while (done == false && index > 0) {

                    /* before we remove rows, calculate some sizes */

                    /* XXX this is ugly, but needed for the way we layout things */

                    row = (TableRow) table.getChildAt(index);
                    if (row != null && row.getVisibility() == VISIBLE) {
                        m_sess.setPatientRowHeight(row.getMeasuredHeight());
                        for (int count = 0; done == false && count < row.getChildCount(); count++) {
                            View cv = row.getChildAt(count);

                            if (cv != null && cv.getVisibility() == VISIBLE) {
                                if (cv instanceof LinearLayout) {

                                    int widthAcc = 0;
                                    ViewGroup vg = (ViewGroup) cv;
                                    boolean sawImage = false;
                                    boolean sawText = false;
                                    for (int i = 0; vg != null && i < vg.getChildCount(); i++) {
                                        View inner = vg.getChildAt(i);
                                        if (inner != null && inner instanceof TextView) {
                                            int w = 0;
                                            w = inner.getWidth();
                                            if (w != 0) {
                                                sawText = true;
                                                widthAcc += inner.getWidth();
                                            }
                                        } else if (inner != null && inner instanceof ImageView) {
                                            int w = 0;
                                            w = inner.getWidth();
                                            if (w != 0) {
                                                sawImage = true;
                                                widthAcc += inner.getWidth();
                                            }
                                        }
                                    }
                                    if (sawImage == true && sawText == true && widthAcc != 0) {
                                        m_sess.setPatientColumnWidth(widthAcc);
                                        done = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    index = index - 1;
                }
                int hAcc = 0;

                View v = findViewById(R.id.statusLinearLayout);
                if (v != null && v.getVisibility() == VISIBLE) {
                    hAcc += v.getPaddingBottom();
                    hAcc += v.getPaddingTop();
                }

                v = findViewById(R.id.tableLayout1);
                if (v != null && v.getVisibility() == VISIBLE) {
                    hAcc += v.getMeasuredHeight();
                }

                v = findViewById(R.id.stationlabels);
                if (v != null && v.getVisibility() == VISIBLE) {
                    hAcc += v.getMeasuredHeight();
                }

                v = findViewById(R.id.lineRow2);
                if (v != null && v.getVisibility() == VISIBLE) {
                    hAcc += v.getMeasuredHeight();
                }

                v = findViewById(R.id.lineRow);
                if (v != null && v.getVisibility() == VISIBLE) {
                    hAcc += v.getMeasuredHeight();
                }

                v = findViewById(R.id.stationheaders);
                if (v != null && v.getVisibility() == VISIBLE) {
                    hAcc += v.getMeasuredHeight();
                }

                v = findViewById(R.id.clinicstatus);
                if (v != null && v.getVisibility() == VISIBLE) {
                    hAcc += v.getMeasuredHeight();
                }

                m_sess.setHeaderHeight(hAcc + 100);  // XXX 100 is a fudge, need to figure out why it is needed

                table.removeViews(4, childCount - 4);
            }
        }

        private void removeTableRowChildren(TableRow trow) {
            if (trow != null) {
                ((ViewGroup) trow).removeAllViews();
            }
        }

        private void displayHeader(final int offset, final int count)
        {
            m_sess.setContext(m_context);
            StationActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    ArrayList<QueueHeader> headers;
                    ArrayList<String> labels;

                    String label = "";
                    int green = ContextCompat.getColor(m_context, R.color.colorGreen);
                    int skyBlue = ContextCompat.getColor(m_context, R.color.skyBlue);
                    int yellow = ContextCompat.getColor(m_context, R.color.colorYellow);

                    headers = m_sess.getStationHeaders(offset, count);

                    TableRow trow = (TableRow) findViewById(R.id.stationheaders);

                    if (trow != null) {
                        removeTableRowChildren(trow);

                        for (int h = 0; h < headers.size(); h++) {
                            TextView b = new TextView(m_context);

                            labels = headers.get(h).getLabels();
                            label = "";
                            for (int i = 0; i < labels.size(); i++) {
                                label = label + labels.get(i);
                                label += "\n";
                            }
                            b.setText(label);
                            QueueHeader.State state = headers.get(h).getState();
                            if (state == QueueHeader.State.WAITING) {
                                b.setTextColor(yellow);
                            } else if (state == QueueHeader.State.AWAY) {
                                b.setTextColor(skyBlue);
                            } else {
                                b.setTextColor(green);
                            }
                            b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            b.setTypeface(null, Typeface.BOLD);
                            b.setGravity(Gravity.CENTER_HORIZONTAL);
                            b.setLines(4);
                            b.setMaxLines(4);
                            b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                            trow.addView(b);
                        }
                   }
                }
            });
        }

        private void displayPage(final int page, final int count)
        {
            m_sess.setContext(m_context);

            StationActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    HideyHelper hh = new HideyHelper();
                    hh.toggleHideyBar(StationActivity.this);
                    TextView text = (TextView) findViewById(R.id.clinicdatetime);
                    text.setText(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date()));

                    TableLayout table = (TableLayout) findViewById(R.id.stationtable);
                    cleanStationTable(table);
                    ArrayList<String> labels;
                    int colorWhite = ContextCompat.getColor(m_context, R.color.colorWhite);
                    int colorGrey = ContextCompat.getColor(m_context, R.color.colorGrey);

                    labels = m_sess.getLabels(page, count);
                    int labelCount = labels.size();

                    TableRow trow = (TableRow) findViewById(R.id.stationlabels);

                    if (trow != null) {
                        removeTableRowChildren(trow);

                        for (int i = 0; i < labelCount; i++) {
                            TextView b = new TextView(m_context);

                            b.setText(labels.get(i));
                            b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                            b.setTypeface(null, Typeface.BOLD);
                            b.setGravity(Gravity.CENTER_HORIZONTAL);
                            b.setTextColor(ContextCompat.getColor(m_context, R.color.colorWhite));
                            b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                            trow.addView(b);
                        }
                    }

                    int numRows = m_sess.getNumberOfRows();
                    int colorCount = 0;
                    for (int i = -1; i < numRows; i++) {
                        ArrayList<RowData> rowdata;
                        if (i == -1) {
                            rowdata = m_sess.getActiveRow(page, count);
                        } else {
                            rowdata = m_sess.getRow(page, i, count);
                        }

                        TableRow tr = new TableRow(m_context);

                        for (int j = 0; j < rowdata.size(); j++) {
                            
                            LinearLayout parent = new LinearLayout(m_context);

                            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                            parent.setOrientation(LinearLayout.HORIZONTAL);
                            parent.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

                            ImageView iv = new ImageView(m_context);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
                            iv.setLayoutParams(layoutParams);

                            RowData rd = rowdata.get(j);
                            String t = rd.getRowdata();
                            if (t.equals("") == false) {
                                if (t.indexOf("Male") >= 0 || t.indexOf("Mascul") >= 0) {
                                    //iv.setImageResource(R.drawable.imageboywhitehalf);
                                    iv.setImageResource(R.drawable.boyfront);
                                } else {
                                    //iv.setImageResource(R.drawable.imagegirlwhitehalf);
                                    iv.setImageResource(R.drawable.girlfront);
                                }
                            }

                            TextView b = new TextView(m_context);
                            b.setTag(rd);
                            b.setOnClickListener(new View.OnClickListener()
                            {
                                public void onClick(View v)
                                {
                                    RowData rdTag = (RowData) v.getTag();
                                    showDeleteDialog(rdTag);
                                }
                            });

                            b.setText(t);
                            b.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                            b.setTypeface(null, Typeface.BOLD);
                            b.setMaxLines(4);
                            b.setLines(4);
                            b.setGravity(Gravity.CENTER_HORIZONTAL);
                            b.setTextColor(ContextCompat.getColor(m_context, R.color.colorBlack));
                            if (i == -1) {
                                b.setBackgroundColor(ContextCompat.getColor(m_context, R.color.colorGreen));
                            } else if (rowdata.get(j).isWaitingItem()) {
                                b.setBackgroundColor(ContextCompat.getColor(m_context, R.color.colorYellow));
                            } else {
                                if (colorCount % 2 == 0) {
                                    b.setBackgroundColor(colorWhite);
                                } else {
                                    b.setBackgroundColor(colorGrey);
                                }
                            }

                            parent.addView(iv);
                            parent.addView(b);

                            parent.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                            tr.addView(parent);
                        }
                        table.addView(tr);
                        colorCount++;
                    }

                    String status = m_sess.getOverallStatus();
                    text = (TextView) findViewById(R.id.clinicstatus);
                    text.setText(status);
                    text.setTextColor(ContextCompat.getColor(m_context, R.color.colorWhite));

                    text = (TextView) findViewById(R.id.title);
                    String title;
                    if (m_sess.getLanguage().equals("en_US")) {
                        title = getString(R.string.clinicstationstatus);
                    } else {
                        title = getString(R.string.clinicstationstatus_es);
                    }
                    text.setText(title);
                }
            });
        }

        public void setContext(Context c)
        {
            m_context = c;
        }

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            // get the string from params, which is an array
            Object lock;
            SessionSingleton sess = SessionSingleton.getInstance();
            int offset = 0;
            int status = -1;
            int numPages = 0;
            int page = 0;

            while (true) {
                if (isCancelled()) {
                    Log.i("doInBackground", "task is cancelled, leaving");
                    break;
                }
                Log.i("Station Activity", "Top of Loop");
                if (page == 0 || m_refresh == true) {
                    m_refresh = false;
                    QueueREST queueData = new QueueREST(m_context);
                    lock = queueData.getQueueData(sess.getClinicId());
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

                    status = queueData.getStatus();

                    if (status == 101) {
                        StationActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                            }
                        });

                    } else if (status == 400) {
                        StationActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_internal_bad_request, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (status == 404) {
                        StationActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_clinic_not_found_date, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (status == 500) {
                        StationActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (status != 200) {
                        StationActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    // following code issues requests to the backend, if they fail, return to top of loop
                    if (status == 200) {
                        if (m_sess.updateClinicStationList() == false) {
                            StationActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.error_clinicstationlist, Toast.LENGTH_LONG).show();
                                }
                            });
                            continue;
                        }
                        if (m_sess.updatePatientList() == false) {
                            StationActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.error_patientlist, Toast.LENGTH_LONG).show();

                                }
                            });
                            continue;
                        }
                        numPages = sess.getPageCount();
                    }
                }
                if (status == 200) {
                    int pageColumnCount = m_sess.getPageColumnCount(page);
                    displayHeader(page, pageColumnCount);
                    displayOverallStatus();
                    displayPage(page, pageColumnCount);

                    long ticks = 20000;
                    while (ticks > 0 && m_swiped == false && m_refresh == false) {
                        try {
                            Thread.sleep(500);
                            ticks -= 500;
                        } catch (InterruptedException e) {
                        }
                    }
                    if (m_refresh == false) {
                        page++;
                    }
                    m_swiped = false;
                    if (page == numPages || m_refresh == true) {
                        m_sess.clearPatientData();
                        if (m_refresh == false) {
                            page = 0;
                            String lang = m_sess.getLanguage();
                            if (lang.equals("en_US")) {
                                m_sess.setLanguage("es_US");
                            } else {
                                m_sess.setLanguage("en_US");
                            }
                        }
                    }
                } else {

                    try {
                        Thread.sleep(5000);
                    } catch(InterruptedException e) {
                    }
                }
            }
            return("");
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Do things like hide the progress bar or change a TextView
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        HideyHelper h = new HideyHelper();
        h.toggleHideyBar(this);

        final ClinicREST clinicREST = new ClinicREST(m_context);

        final Object lock;

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        lock = clinicREST.getClinicData(year, month, day);

        final Thread thread = new Thread() {
            public void run() {
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

                SessionSingleton data = SessionSingleton.getInstance();
                int status = clinicREST.getStatus();
                if (status == 200) {
                    m_task = new GetAndDisplayTask();
                    m_task.setContext(getApplicationContext());
                    m_task.execute();
                    return;
                } else if (status == 101) {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                        }
                    });

                } else if (status == 400) {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.error_internal_bad_request, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (status == 404) {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.error_clinic_not_found_date, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (status == 500) {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    StationActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_station_status);
        m_context = this.getApplicationContext();
        m_detector = new GestureDetectorCompat(this, new GestureListener());
    }

    @Override
    public void onBackPressed() {
        if(m_task!=null){
            m_task.cancel(true);
            m_task = null;
        }
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    void showDeleteDialog(RowData rd)
    {
        DeleteFromQueueDialogFragment rtc = new DeleteFromQueueDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(null, rd);
        rtc.setArguments(args);
        rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.msg_delete));
    }
}

