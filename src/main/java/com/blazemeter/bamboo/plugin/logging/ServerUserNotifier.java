/**
 * Copyright 2017 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazemeter.bamboo.plugin.logging;

import com.blazemeter.api.logging.UserNotifier;
import org.apache.log4j.Logger;

public class ServerUserNotifier implements UserNotifier {

    private static final Logger logger = Logger.getLogger(ServerUserNotifier.class);

    @Override
    public void notifyInfo(String info) {
        logger.info(info);
    }

    @Override
    public void notifyWarning(String warn) {
        logger.warn(warn);
    }

    @Override
    public void notifyError(String error) {
        logger.error(error);
    }
}
