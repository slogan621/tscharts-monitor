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

package org.thousandsmiles.thousandsmilesmonitor;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ActionDialogAdapter extends BaseAdapter {

    public enum PatientOp
    {
        RemoveFromXRay, DeletePatientFromClinic, ViewPatientData, EditOldChartId
    }

    private ArrayList<Integer> m_actionIds = new ArrayList<Integer>();
    private ArrayList<Integer> m_actionTextIds = new ArrayList<Integer>();
    private boolean m_isXray = false;
    private boolean m_isActiveRow = false;
    private HashMap<PatientOp, Integer> m_opMap = new HashMap<PatientOp, Integer>();

    public void initialize(boolean isXray, boolean isActiveRow) {
        m_isXray = isXray;
        int offset = 0;
        m_isActiveRow = isActiveRow;
        if (m_isXray == true && m_isActiveRow == false) {
            m_opMap.put(PatientOp.RemoveFromXRay, offset);
            offset++;
            m_actionIds.add(R.drawable.xray_selector);
            m_actionTextIds.add(R.string.msg_button_remove_from_xray_queue);
        }
        if (m_isActiveRow == false) {
            m_opMap.put(PatientOp.DeletePatientFromClinic, offset);
            offset++;
            m_actionIds.add(R.drawable.delete_selector);
            m_actionTextIds.add(R.string.msg_button_delete_from_clinic);
        }
        m_actionIds.add(R.drawable.view_summary_selector);
        m_actionTextIds.add(R.string.msg_button_view_patient_summary);
        m_opMap.put(PatientOp.ViewPatientData, offset);
        offset++;
        m_actionIds.add(R.drawable.edit_oldid_selector);
        m_actionTextIds.add(R.string.msg_button_edit_old_id);
        m_opMap.put(PatientOp.EditOldChartId, offset);
        offset++;
    }

    public int getPosition(PatientOp op) {
        int ret = -1;    // not found
        try {
            ret = m_opMap.get(op);
        } catch (Exception e) {
            ret = -1;
        }
        return ret;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout btnLO = new LinearLayout(parent.getContext());

        LinearLayout.LayoutParams paramsLO = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        paramsLO.setMargins(0, 0, 0, 0);

        btnLO.setOrientation(LinearLayout.VERTICAL);
        btnLO.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView i = new ImageView(parent.getContext());

        i.setImageResource(m_actionIds.get(position));
        ((ImageView) i).setScaleType(ImageView.ScaleType.FIT_CENTER);
        final int w = (int) (36 * parent.getResources().getDisplayMetrics().density + 0.5f);
        i.setLayoutParams(new GridView.LayoutParams(w * 2, w * 2));
        btnLO.addView(i, paramsLO);

        TextView tv=new TextView(parent.getContext());
        tv.setText(parent.getContext().getString(m_actionTextIds.get(position)));
        btnLO.addView(tv, paramsLO);

        return (View) btnLO;
    }

    public final int getCount() {
        int ret = m_actionIds.size();
        return ret;
    }

    public final Object getItem(int position) {
        return null;
    }

    public final long getItemId(int position) {
        return position;
    }
}
