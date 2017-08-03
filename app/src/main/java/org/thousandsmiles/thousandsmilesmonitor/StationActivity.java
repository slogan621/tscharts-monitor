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

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class StationActivity extends Activity {
    private static final int TIME_OUT = 15000;
    Context context;

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

        private void cleanTable(TableLayout table) {

            int childCount = table.getChildCount();

            // Remove all rows except the headers and other static rows near the top
            if (childCount > 4) {
                table.removeViews(4, childCount - 4);
            }
        }

        private void displayHeader(final int offset)
        {
            m_sess.setContext(m_context);
            StationActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    ArrayList<QueueHeader> headers;
                    int [] headerIds = {
                            R.id.stationheader1,
                            R.id.stationheader2,
                            R.id.stationheader3,
                            R.id.stationheader4,
                            R.id.stationheader5,
                    };
                    QueueHeader item;
                    ArrayList<String> labels;
                    String label = "";
                    int green = ContextCompat.getColor(m_context, R.color.colorGreen);
                    int skyBlue = ContextCompat.getColor(m_context, R.color.skyBlue);
                    int yellow = ContextCompat.getColor(m_context, R.color.colorYellow);

                    headers = m_sess.getStationHeaders(offset);

                    TextView text;

                    for (int h = 0; h < headerIds.length; h++) {
                        text = (TextView) findViewById(headerIds[h]);
                        text.setText("");
                    }

                    for (int h = 0; h < headers.size(); h++) {
                        text = (TextView) findViewById(headerIds[h]);
                        labels = headers.get(h).getLabels();
                        label = "";
                        for (int i = 0; i < labels.size(); i++) {
                            label = label + labels.get(i);
                            label += "\n";
                        }
                        text.setText(label);
                        QueueHeader.State state = headers.get(h).getState();
                        if (state == QueueHeader.State.WAITING) {
                            text.setTextColor(green);
                        } else if (state == QueueHeader.State.AWAY) {
                            text.setTextColor(skyBlue);
                        } else {
                            text.setTextColor(yellow);
                        }
                    }
                 }
            });
        }

        private void displayPage(final int offset)
        {
            m_sess.setContext(m_context);
            StationActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    TextView text = (TextView) findViewById(R.id.clinicdatetime);
                    text.setText(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date()));

                    TableLayout table = (TableLayout) findViewById(R.id.stationtable);
                    cleanTable(table);
                    ArrayList<String> labels;

                    labels = m_sess.getLabels(offset);
                    int len = labels.size();

                    text = (TextView) findViewById(R.id.stationlabel1);
                    text.setText(labels.get(0));
                    text = (TextView) findViewById(R.id.stationlabel2);
                    text.setText(labels.get(1));
                    text = (TextView) findViewById(R.id.stationlabel3);
                    text.setText(labels.get(2));
                    text = (TextView) findViewById(R.id.stationlabel4);
                    text.setText(labels.get(3));
                    text = (TextView) findViewById(R.id.stationlabel5);
                    text.setText(labels.get(4));

                    int numRows = m_sess.getNumberOfRows();
                    for (int i = 0; i < numRows; i++) {
                        ArrayList<String> rowdata = m_sess.getRow(offset, i);

                        TableRow tr = new TableRow(m_context);
                        for (int j = 0; j < rowdata.size(); j++) {

                            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                            LinearLayout parent = new LinearLayout(m_context);

                            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            parent.setOrientation(LinearLayout.HORIZONTAL);

                            ImageView iv = new ImageView(m_context);

                            String t = rowdata.get(j);
                            if (t.equals("") == false) {
                                if (t.indexOf("Male") >= 0 || t.indexOf("Mascul") >= 0) {
                                    iv.setImageResource(R.drawable.imageboywhitehalf);
                                } else {
                                    iv.setImageResource(R.drawable.imagegirlwhitehalf);
                                }
                            }

                            TextView b = new TextView(m_context);

                            b.setText(rowdata.get(j));
                            b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            b.setTypeface(null, Typeface.BOLD);
                            b.setMaxLines(4);
                            b.setLines(4);
                            b.setGravity(Gravity.CENTER_HORIZONTAL);

                            parent.addView(iv);
                            parent.addView(b);

                            TableRow.LayoutParams params = (TableRow.LayoutParams)tr.getLayoutParams();
                            params.weight = 1;
                            params.width=0;
                            params.gravity = Gravity.CENTER_HORIZONTAL;
                            parent.setLayoutParams(params);
                            tr.addView(parent);
                        }
                        table.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
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
            int len;
            int count = 0;
            int status = -1;
            int numPages = 0;

            while (true) {
                if (count == 0) {
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
                    if (status == 200) {
                        numPages = sess.getPageCount();
                    }

                }
                if (status == 200) {
                    final int aCount = count;
                    m_sess.updatePatientList();
                    m_sess.updateClinicStationList();
                    displayHeader(aCount);
                    displayOverallStatus();
                    displayPage(aCount);

                    try {
                        Thread.sleep(15000);
                    } catch(InterruptedException e) {
                    }

                    count += sess.getPageSize();
                    if (count >= numPages * sess.getPageSize()) {
                        count = 0;
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_station_status);
        context = this.getApplicationContext();

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
                    GetAndDisplayTask t = new GetAndDisplayTask();
                    t.setContext(getApplicationContext());
                    t.execute();
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
}

