package com.stressfreeroads.gradproject;


import android.app.Activity;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapTrafficLayer;
import com.here.android.mpa.mapping.MapTransitLayer;

/**
 * This class encapsulates the properties and functionality of the settings panel,which provides the
 * UI elements to control the map attributes.
 */

public class SettingsPanel {
    // Initialize UI elements
    private RadioGroup m_mapModeGroup;

//    private Switch m_flowSwitch;
//    private Switch m_incidentSwitch;

    private Activity m_activity;
    private Map m_map;

    public SettingsPanel(Activity activity, Map map) {
        m_activity = activity;
        m_map = map;
        initUIElements();
    }

    private void initUIElements() {
        m_mapModeGroup = (RadioGroup) m_activity.findViewById(R.id.mapModeRadioGroup);
//        m_flowSwitch = (Switch) m_activity.findViewById(R.id.flowSwitch);
//        m_incidentSwitch = (Switch) m_activity.findViewById(R.id.incidentSwitch);

        setUIListeners();
    }

    /**
     * Change map scheme as selected option.
     */
    private void setUIListeners() {
        m_mapModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    /*
                     * Please refer to javadoc or call Map.getMapSchemes() for all supported map
                     * schemes
                     */
                    case R.id.mapModeBtn:
                        m_map.setMapScheme(Map.Scheme.NORMAL_DAY);
                        break;
                    case R.id.hybridModeBtn:
                        m_map.setMapScheme(Map.Scheme.HYBRID_DAY);
                        break;
                    case R.id.terrainModeBtn:
                        m_map.setMapScheme(Map.Scheme.TERRAIN_DAY);
                        break;
                    default:
                }
            }
        });

        /**
         * Enable or disable FLOW map traffic layer.
         */
//        m_flowSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                /* TrafficInfo has to be turned on first */
//                m_map.setTrafficInfoVisible(isChecked);
//                m_map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.FLOW, isChecked);
//                m_map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.ONROUTE,isChecked);
//            }
//        });
//        /**
//         * Enable or disable INCIDENT map traffic layer.
//         */
//        m_incidentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                /* TrafficInfo has to be turned on first */
//                m_map.setTrafficInfoVisible(isChecked);
//                m_map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.INCIDENT,isChecked);
//
//            }
//        });

    }
}