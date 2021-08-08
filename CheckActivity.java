   public void encryptProfile(String name, String birth, String password, String orgCode, String region){
        Log.d("test", "L0O 암호화 시작중");
        writeLog("Whoosh! Starting user data encryption");
        final WebView webView2 = new WebView(getApplicationContext());
        webView2.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public void sendJson(String json){
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    enc_name = jsonObject.getString("name");
                    enc_birth = jsonObject.getString("birth");
                    enc_password = jsonObject.getString("password");
                    if(!enc_name.isEmpty() && !enc_birth.isEmpty() && !enc_password.isEmpty()) {
                        //성공
                        Log.d("test", "L0O name " + enc_name);
                        Log.d("test", "L0O birth " + enc_birth);
                        Log.d("test", "L0O pw " + enc_password);
                        encryptSuccess = true;
                        writeLog("User data encrypt... OK");
                        findUser(region, orgCode, enc_name, enc_birth);//지역이랑, 학교 코드
                    }
                } catch (JSONException e) {
                    Toast.makeText(CheckActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }, "Android");
        webSettings = webView2.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        Log.d("test", "L0O URL 로드중");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!encryptSuccess){
                    try {
                        writeLog("Encryption failed! restarting load user data...");
                        Log.d("test", "L0O URL 로드 재시도중...");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                webView2.loadUrl("file:///android_asset/enc.html?name=" + name + "&birth=" + birth +"&password=" + password);
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void findUser(String region, String orgCode, String enc_name, String enc_birth){
        Log.d("test", "L0O findUser");
        writeLog("Finding user");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String requestUrl = "https://" + region + "hcs.eduro.go.kr/v2/findUser";

        StringRequest encryptRequest = new StringRequest(Request.Method.POST, requestUrl, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.d("test", "L0O " + response);
                try {
                    writeLog("GET | User Token... OK");
                    JSONObject jsonObject = new JSONObject(response);
                    enc_token = jsonObject.getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                hasPassword(region, enc_token);
            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("test", "L0O " + error.toString());
                        Toast.makeText(CheckActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Nullable

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/json");
                return params;
            }


            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("birthday", enc_birth);
                    jsonObject.put("loginType", "school");
                    jsonObject.put("name", enc_name);
                    jsonObject.put("orgCode", orgCode);
                    jsonObject.put("stdntPNo", null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("test", "L0O " + jsonObject.toString());
                return jsonObject.toString().getBytes();
            }
        };
        encryptRequest.setTag("ProfileRequest");
        queue.add(encryptRequest);
    }
    private void hasPassword(String region, String enc_token){
        Log.d("test", "L0O hasPassword");
        writeLog("Checking account password");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String requestUrl = "https://" + region + "hcs.eduro.go.kr/v2/hasPassword";

        StringRequest encryptRequest = new StringRequest(Request.Method.POST, requestUrl, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                if(response.equals("true")){
                    writeLog("Check account password... OK");
                    validatePassword(region);
                } else {
                    writeLog("Password not set. msg : 자가진단 사이트에서 사용자 비밀번호를 설정하세요");
                    Log.d("test", "L0O 비번 없음");
                }
            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("test", "L0O " + error.toString());
                        Toast.makeText(CheckActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Nullable

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization",enc_token);
                params.put("Content-Type", "application/json");
                return params;
            }

        };
        encryptRequest.setTag("ProfileRequest");
        queue.add(encryptRequest);
    }

    private void validatePassword(String region){
        Log.d("test", "L0O validatePassword");
        writeLog("Validating encrypted password");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String requestUrl = "https://" + region + "hcs.eduro.go.kr/v2/validatePassword";

        StringRequest encryptRequest = new StringRequest(Request.Method.POST, requestUrl, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                writeLog("Encrypted password... OK");
                Log.d("test", "L0O validate " + response);
                enc_token= response.replace("\"","");
                Log.d("test", "L0O replaced " + enc_token);
                selectUserGroup(region, enc_token);
            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //비번 틀림
                        Log.d("test", "L0O " + error.toString());
                        Toast.makeText(CheckActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Nullable

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization",enc_token);
                params.put("Content-Type", "application/json");
                return params;
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("deviceUuid", "");
                    jsonObject.put("password", enc_password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("test", "L0O " + jsonObject.toString());
                return jsonObject.toString().getBytes();
            }
        };
        encryptRequest.setTag("ProfileRequest");
        queue.add(encryptRequest);
    }

    private void selectUserGroup(String region, String enc_token){
        Log.d("test", "L0O selectUserGrouop");
        writeLog("Checking first user info");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String requestUrl = "https://" + region + "hcs.eduro.go.kr/v2/selectUserGroup";

        StringRequest encryptRequest = new StringRequest(Request.Method.POST, requestUrl, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.d("test", "L0O " + response);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    //다중사용자 처리 (수정 필요)
                    if(pref.getBooleanData(getApplicationContext(), "multipleUser", false)){
                        for(int i =0; i < jsonArray.length(); i++){
                            JSONObject tempObject = jsonArray.getJSONObject(i);
                            writeLog("Getting Multiple User Info... " + tempObject.getString("userPNo") + " | " + tempObject.getString("userPNo"));
                            Log.d("test", "L0O Multi UserList[" + i + "] " +  tempObject.getString("userPNo") + " | " + tempObject.getString("userPNo"));
                            getUserInfo(region, tempObject.getString("userPNo"), tempObject.getString("orgCode"), tempObject.getString("token"));
                        }
                    } else {
                        JSONObject tempObject = jsonArray.getJSONObject(0);
                        Log.d("test", "L0O UserList[0] " +  tempObject.getString("userPNo") + " | " + tempObject.getString("userPNo"));
                        writeLog("First User Selected... " + tempObject.getString("userPNo") + " | " + tempObject.getString("userPNo"));
                        getUserInfo(region, tempObject.getString("userPNo"), tempObject.getString("orgCode"), tempObject.getString("token"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //비번 틀림
                        Log.d("test", "L0O " + error.toString());
                        Toast.makeText(CheckActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Nullable

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization",enc_token);
                params.put("Content-Type", "application/json");
                params.put("Referer", "https://hcs.eduro.go.kr/");
                params.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15");
                params.put("Origin", "https://hcs.eduro.go.kr");
                return params;
            }
        };
        encryptRequest.setTag("UserGroup");
        queue.add(encryptRequest);
    }

    private void getUserInfo(String region, String userNo, String orgCode, String token){
        writeLog("Validating selected user info");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String requestUrl = "https://" + region + "hcs.eduro.go.kr/v2/getUserInfo";
        StringRequest encryptRequest = new StringRequest(Request.Method.POST, requestUrl, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.d("test", "L0O getUserInfo " + response);
                try {
                    writeLog("GET | User token, User nameEnc... OK");
                    JSONObject jsonObject = new JSONObject(response);
                    registerSurvey(region, jsonObject.getString("token"), jsonObject.getString("userNameEncpt"));
                    Log.d("test", "L0O getUserInfo token " + jsonObject.getString("token"));
                    Log.d("test", "L0O getUserInfo name " + jsonObject.getString("userNameEncpt"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //비번 틀림
                        Log.d("test", "L0O " + error.toString());
                        Toast.makeText(CheckActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Nullable

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization",token);
                params.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15");
                params.put("Content-Type", "application/json");
                params.put("Referer", "https://hcs.eduro.go.kr/");
                params.put("Origin", "https://hcs.eduro.go.kr");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("orgCode", orgCode);
                    jsonObject.put("userPNo", userNo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject.toString().getBytes();
            }
        };
        encryptRequest.setTag("getUserInfo");
        queue.add(encryptRequest);
    }

    private void registerSurvey(String region, String token, String name){
        writeLog("Ready for register survey");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String requestUrl = "https://" + region + "hcs.eduro.go.kr/registerServey";

        StringRequest encryptRequest = new StringRequest(Request.Method.POST, requestUrl, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                writeLog("Checking survey response data... OK");
                Log.d("test", "L0O registerSurvey  " + response);
                if(!response.isEmpty()){
                    writeLog("Muyaho! survey success");
                    Toast.makeText(CheckActivity.this, "일일 자가진단을 완료하였습니다!", Toast.LENGTH_SHORT).show();
                    writeLog(getApplicationContext(), "OneClick 자가진단 완료");
                    finish();
                } else {
                    writeLog("Failed Survey! Response message : 자가진단에 실패하였거나 성공 여부를 확인할 수 없습니다.");
                    Toast.makeText(CheckActivity.this, "자가진단 여부를 확인할 수 없습니다\n다시 시도해주세요", Toast.LENGTH_LONG).show();
                    finish();
                }
                writeLog("--------------------------");
            }
        },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //비번 틀림
                        Log.d("test", "L0O registerSurvey" + error.toString());
                        Log.d("test", "L0O registerSurvey" + error.networkResponse.statusCode);
                        Log.d("test", "L0O registerSurvey" + error.networkResponse.headers);
                        writeLog("Failed Survey! Response code | " + error.networkResponse.statusCode);
                        Toast.makeText(CheckActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Nullable

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization",token);
                params.put("Content-Type", "application/json");
                params.put("Referer", "https://hcs.eduro.go.kr/");
                params.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15");
                params.put("Origin", "https://hcs.eduro.go.kr");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject surveyJson = new JSONObject();
                try {
                    surveyJson.put("deviceUuid", "");
                    surveyJson.put("rspns00", "Y");
                    surveyJson.put("rspns01", "1");
                    surveyJson.put("rspns02", "1");
                    surveyJson.put("rspns03", null);
                    surveyJson.put("rspns04", null);
                    surveyJson.put("rspns05", null);
                    surveyJson.put("rspns06", null);
                    surveyJson.put("rspns07", null);
                    surveyJson.put("rspns08", null);
                    surveyJson.put("rspns09", "0");
                    surveyJson.put("rspns10", null);
                    surveyJson.put("rspns11", null);
                    surveyJson.put("rspns12", null);
                    surveyJson.put("rspns13", null);
                    surveyJson.put("rspns14", null);
                    surveyJson.put("rspns15", null);
                    surveyJson.put("upperToken", token);
                    surveyJson.put("upperUserNameEncpt", name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return surveyJson.toString().getBytes();
            }
        };
        encryptRequest.setTag("getUserInfo");
        queue.add(encryptRequest);
    }
