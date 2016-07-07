/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.blazemeter.bamboo;

public interface TestConstants {
    String RESOURCES = System.getProperty("user.dir")+"/src/test/java/com/blazemeter/bamboo/resources";


    // Mocked API constants
    String MOCKED_USER_KEY_VALID ="mockedAPIKeyValid";
    String MOCKED_USER_KEY_RETRIES ="mockedAPIKeyRetries";
    String MOCKED_USER_KEY_TEST_TYPE ="mockedAPIKeyTestType";
    String MOCKED_USER_KEY_V2 ="mockedAPIKeyV2";
    String MOCKED_USER_KEY_6_TESTS ="mockedAPIKeyValid-1-tests";
    String MOCKED_USER_KEY_1_TEST ="mockedAPIKeyValid-1-test";
    String MOCKED_USER_KEY_0_TESTS ="mockedAPIKeyValid-0-tests";
    String MOCKED_USER_KEY_INVALID ="mockedAPIKeyInValid";
    String MOCKED_USER_KEY_EXCEPTION ="mockedAPIKeyException";
    int mockedApiPort=1234;
    String proxyPort="2345";
    String mockedApiUrl="http://127.0.0.1:"+mockedApiPort;

    String TEST_MASTER_ID ="testMasterId";
    String TEST_MASTER_NOT_FOUND ="testMaster-not-found";
    String TEST_MASTER_0 ="testMaster-0";
    String TEST_MASTER_25 ="testMaster-25";
    String TEST_MASTER_70 ="testMaster-70";
    String TEST_MASTER_100 ="testMaster-100";
    String TEST_MASTER_140 ="testMaster-140";
    String TEST_MASTER_SUCCESS ="testMasterSuccess";
    String TEST_MASTER_FAILURE ="testMasterFailure";
    String TEST_MASTER_ERROR ="testMasterError";

    String YAHOO="yahoo";
}
