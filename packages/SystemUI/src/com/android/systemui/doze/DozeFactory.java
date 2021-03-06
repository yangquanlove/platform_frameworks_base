/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.doze;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;

import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.util.wakelock.WakeLock;

public class DozeFactory {

    public DozeFactory() {
    }

    /** Creates a DozeMachine with its parts for {@code dozeService}. */
    public DozeMachine assembleMachine(DozeService dozeService) {
        Context context = dozeService;
        SensorManager sensorManager = context.getSystemService(SensorManager.class);
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        DozeHost host = getHost(dozeService);
        AmbientDisplayConfiguration config = new AmbientDisplayConfiguration(context);
        DozeParameters params = new DozeParameters(context);
        Handler handler = new Handler();
        WakeLock wakeLock = WakeLock.createPartial(context, "Doze");

        DozeMachine machine = new DozeMachine(
                DozeScreenStatePreventingAdapter.wrapIfNeeded(dozeService, params),
                config,
                wakeLock);
        machine.setParts(new DozeMachine.Part[]{
                createDozeTriggers(context, sensorManager, host, config, params, handler, wakeLock,
                        machine),
                createDozeUi(context, host, wakeLock, machine, handler, alarmManager),
        });

        return machine;
    }

    private DozeTriggers createDozeTriggers(Context context, SensorManager sensorManager,
            DozeHost host, AmbientDisplayConfiguration config, DozeParameters params,
            Handler handler, WakeLock wakeLock, DozeMachine machine) {
        boolean allowPulseTriggers = true;
        return new DozeTriggers(context, machine, host, config, params,
                sensorManager, handler, wakeLock, allowPulseTriggers);
    }

    private DozeMachine.Part createDozeUi(Context context, DozeHost host, WakeLock wakeLock,
            DozeMachine machine, Handler handler, AlarmManager alarmManager) {
        return new DozeUi(context, alarmManager, machine, wakeLock, host, handler);
    }

    public static DozeHost getHost(DozeService service) {
        Application appCandidate = service.getApplication();
        final SystemUIApplication app = (SystemUIApplication) appCandidate;
        return app.getComponent(DozeHost.class);
    }
}
