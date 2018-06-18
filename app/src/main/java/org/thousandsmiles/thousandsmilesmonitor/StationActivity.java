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
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
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

public class StationActivity extends Activity {
    private static final int TIME_OUT = 15000;
    Context context;
    GetAndDisplayTask m_task = null;

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
                if (childCount > 4) {
                    table.removeViews(4, childCount - 4);
                }
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

        private void displayPage(final int offset, final int count)
        {
            m_sess.setContext(m_context);
            StationActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    TextView text = (TextView) findViewById(R.id.clinicdatetime);
                    text.setText(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date()));

                    TableLayout table = (TableLayout) findViewById(R.id.stationtable);
                    cleanStationTable(table);
                    ArrayList<String> labels;
                    int colorWhite = ContextCompat.getColor(m_context, R.color.colorWhite);
                    int colorGrey = ContextCompat.getColor(m_context, R.color.colorGrey);

                    labels = m_sess.getLabels(offset, count);
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
                        ArrayList<String> rowdata;
                        if (i == -1) {
                            rowdata = m_sess.getActiveRow(offset, count);
                        } else {
                            rowdata = m_sess.getRow(offset, i, count);
                        }

                        TableRow tr = new TableRow(m_context);

                        for (int j = 0; j < rowdata.size(); j++) {
                            
                            LinearLayout parent = new LinearLayout(m_context);

                            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                            parent.setOrientation(LinearLayout.HORIZONTAL);
                            parent.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

                            ImageView iv = new ImageView(m_context);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
                            iv.setLayoutParams(layoutParams);

                            String t = rowdata.get(j);
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

                            b.setText(t);
                            b.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                            b.setTypeface(null, Typeface.BOLD);
                            b.setMaxLines(4);
                            b.setLines(4);
                            b.setGravity(Gravity.CENTER_HORIZONTAL);
                            if (i == -1) {
                                b.setBackgroundColor(ContextCompat.getColor(m_context, R.color.colorGreen));
                                b.setTextColor(ContextCompat.getColor(m_context, R.color.colorBlack));
                            } else if (i == 0 && j == 0) {
                                b.setBackgroundColor(ContextCompat.getColor(m_context, R.color.colorYellow));
                                b.setTextColor(ContextCompat.getColor(m_context, R.color.colorBlack));
                            } else {
                                if (colorCount % 2 == 0) {
                                    b.setBackgroundColor(colorWhite);
                                    b.setTextColor(ContextCompat.getColor(m_context, R.color.colorBlack));
                                } else {
                                    b.setBackgroundColor(colorGrey);
                                    b.setTextColor(ContextCompat.getColor(m_context, R.color.colorBlack));
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
            m_context = context;
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
                if (page == 0) {
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
                    offset = m_sess.getFirstQueueThisPage(page);
                    displayHeader(offset, pageColumnCount);
                    displayPage(offset, pageColumnCount);
                    displayOverallStatus();

                    try {
                        Thread.sleep(20000);
                    } catch(InterruptedException e) {
                    }

                    page++;
                    if (page == numPages) {
                        m_sess.clearPatientData();
                        page = 0;
                        String lang = m_sess.getLanguage();
                        if (lang.equals("en_US")) {
                            m_sess.setLanguage("es_US");
                        } else {
                            m_sess.setLanguage("en_US");
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

        final ClinicREST clinicREST = new ClinicREST(context);

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
        context = this.getApplicationContext();
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
}

