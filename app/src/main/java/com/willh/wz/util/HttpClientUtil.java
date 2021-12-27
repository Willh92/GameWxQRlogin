package com.willh.wz.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class HttpClientUtil {

    private static final int CONNECTION_TIMEOUT = 10000;

    public static String doGet(String serverUrl) {
        if (serverUrl.startsWith("https://")) {
            return doHttpsGet(serverUrl);
        } else {
            return serverUrl.startsWith("http://") ? doHttpGet(serverUrl) : null;
        }
    }

    public static String doPost(String serverUrl, String data) {
        if (serverUrl.startsWith("https://")) {
            return doHttpsPost(serverUrl, data);
        } else {
            return serverUrl.startsWith("http://") ? doHttpPost(serverUrl, data) : null;
        }
    }

    private static String doHttpGet(String serverURL) {
        HttpURLConnection connection = null;
        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufr = null;

        try {
            URL url = new URL(serverURL);
            URLConnection urlConnection = url.openConnection();
            StringBuilder response;
            if (!(urlConnection instanceof HttpURLConnection)) {
                return null;
            } else {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(CONNECTION_TIMEOUT);
                connection.connect();
                int responseCode = connection.getResponseCode();
                String line;
                if (responseCode != 200) {
                    in = connection.getErrorStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufr = new BufferedReader(inputStreamReader);
                    response = new StringBuilder();
                    while ((line = bufr.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("StatusCode", responseCode);
                    jsonObject.put("ResponseStr", response.toString());
                    return jsonObject.toString();
                } else {
                    in = connection.getInputStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufr = new BufferedReader(inputStreamReader);
                    response = new StringBuilder();
                    while ((line = bufr.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            }
        } catch (Exception ignore) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ignore) {
                }
            }

            if (bufr != null) {
                try {
                    bufr.close();
                } catch (IOException ignore) {
                }
            }

            if (connection != null) {
                connection.disconnect();
            }

        }
    }

    private static String doHttpsGet(String serverURL) {
        HttpsURLConnection connection = null;
        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufr = null;

        try {
            URL url = new URL(serverURL);
            URLConnection urlConnection = url.openConnection();
            StringBuilder response;
            if (!(urlConnection instanceof HttpsURLConnection)) {
                return null;
            } else {
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(CONNECTION_TIMEOUT);
                connection.connect();
                int responseCode = connection.getResponseCode();
                String line;
                if (responseCode != 200) {
                    in = connection.getErrorStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufr = new BufferedReader(inputStreamReader);
                    response = new StringBuilder();
                    while ((line = bufr.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("StatusCode", responseCode);
                    jsonObject.put("ResponseStr", response.toString());
                    return jsonObject.toString();
                } else {
                    in = connection.getInputStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufr = new BufferedReader(inputStreamReader);
                    response = new StringBuilder();
                    while ((line = bufr.readLine()) != null) {
                        response.append(line);
                    }

                    return response.toString();
                }
            }
        } catch (Exception ig) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ignore) {
                }
            }

            if (bufr != null) {
                try {
                    bufr.close();
                } catch (IOException ignore) {
                }
            }

            if (connection != null) {
                connection.disconnect();
            }

        }
    }

    private static String doHttpPost(String serverURL, String data) {
        HttpURLConnection connection = null;
        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufr = null;

        try {
            URL url = new URL(serverURL);
            URLConnection urlConnection = url.openConnection();
            OutputStream os;
            if (!(urlConnection instanceof HttpURLConnection)) {
                return null;
            } else {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(CONNECTION_TIMEOUT);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                os = connection.getOutputStream();
                os.write(data.getBytes());
                connection.connect();
                StringBuilder response;
                int responseCode = connection.getResponseCode();
                String line;
                if (responseCode == 200) {
                    in = connection.getInputStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufr = new BufferedReader(inputStreamReader);
                    response = new StringBuilder();
                    while ((line = bufr.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                } else {
                    in = connection.getErrorStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufr = new BufferedReader(inputStreamReader);
                    response = new StringBuilder();

                    while ((line = bufr.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("StatusCode", responseCode);
                    jsonObject.put("ResponseStr", response.toString());
                    return jsonObject.toString();
                }
            }
        } catch (Exception ignore) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ignore) {
                }
            }

            if (bufr != null) {
                try {
                    bufr.close();
                } catch (IOException ignore) {
                }
            }

            if (connection != null) {
                connection.disconnect();
            }

        }
    }

    private static String doHttpsPost(String serverURL, String data) {
        HttpsURLConnection connection = null;
        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufr = null;

        try {
            URL url = new URL(serverURL);
            URLConnection urlConnection = url.openConnection();
            OutputStream os;
            if (!(urlConnection instanceof HttpsURLConnection)) {
                return null;
            } else {
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(CONNECTION_TIMEOUT);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                os = connection.getOutputStream();
                os.write(data.getBytes());
                connection.connect();
                StringBuilder response;
                int responseCode = connection.getResponseCode();
                String line;
                if (responseCode == 200) {
                    in = connection.getInputStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufr = new BufferedReader(inputStreamReader);
                    response = new StringBuilder();

                    while ((line = bufr.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                } else {
                    in = connection.getErrorStream();
                    inputStreamReader = new InputStreamReader(in);
                    bufr = new BufferedReader(inputStreamReader);
                    response = new StringBuilder();

                    while ((line = bufr.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("StatusCode", responseCode);
                    jsonObject.put("ResponseStr", response.toString());
                    return jsonObject.toString();
                }
            }
        } catch (Exception ignore) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ignore) {
                }
            }

            if (bufr != null) {
                try {
                    bufr.close();
                } catch (IOException ignore) {
                }
            }

            if (connection != null) {
                connection.disconnect();
            }

        }
    }
}

