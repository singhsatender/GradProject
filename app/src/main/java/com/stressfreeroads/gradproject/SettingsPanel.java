package com.stressfreeroads.gradproject;


import android.app.Activity;
import android.widget.RadioGroup;

import com.here.android.mpa.mapping.Map;

/**
 * This class encapsulates the properties and functionality of the settings panel,which provides the
 * UI elements to control the map attributes.
 */

public class SettingsPanel {
    // Initialize UI elements
    private RadioGroup m_mapModeGroup;
    private Activity m_activity;
    private Map m_map;

    public SettingsPanel(Activity activity, Map map) {
        m_activity = activity;
        m_map = map;
        initUIElements();
    }

    private void initUIElements() {
        m_mapModeGroup = (RadioGroup) m_activity.findViewById(R.id.mapModeRadioGroup);
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


    }
}