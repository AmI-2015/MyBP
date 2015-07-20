//package com.dev.ami2015.mybikeplace.tasks;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.nfc.NdefMessage;
//import android.nfc.NdefRecord;
//import android.nfc.Tag;
//import android.nfc.tech.Ndef;
//import android.os.AsyncTask;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.dev.ami2015.mybikeplace.R;
//import com.dev.ami2015.mybikeplace.SignInActivity;
//
//import java.io.UnsupportedEncodingException;
//import java.util.Arrays;
//
//
///**
// * Created by Zephyr on 20/07/2015.
// */
//
//public class NdefReaderTask extends AsyncTask<Tag, Void, String> {
//
//    SignInActivity parentActivity = null;
//    SharedPreferences userSettings = null;
//
//    public NdefReaderTask(SignInActivity parentActivity) {
//        this.parentActivity = parentActivity;
//
//        //Creating shared preference file
//        userSettings = this.parentActivity.getSharedPreferences(this.parentActivity.getString(R.string.USER_SETTINGS), Context.MODE_PRIVATE);
//
//    }
//
//    @Override
//    protected String doInBackground(Tag... params) {
//        Tag tag = params[0];
//
//        Ndef ndef = Ndef.get(tag);
//        if (ndef == null) {
//            // NDEF is not supported by this Tag.
//            return null;
//        }
//
//        NdefMessage ndefMessage = ndef.getCachedNdefMessage();
//
//        NdefRecord[] records = ndefMessage.getRecords();
//        for (NdefRecord ndefRecord : records) {
//            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
//                return readText(ndefRecord);
//            }
//        }
//
//        return null;
//    }
//
//    public String readText(NdefRecord ndefRecord)
//    {
//        String tagContent = null;
//        try {
//            byte[] payload = ndefRecord.getPayload();
//            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
//            int languageSize = payload[0] & 0063;
//            tagContent = new String(payload, languageSize + 1,
//                    payload.length - languageSize - 1, textEncoding);
//        } catch (UnsupportedEncodingException e) {
//            Log.e("getTextFromNdefRecord", e.getMessage(), e);
//        }
//        return tagContent;
//    }
//
//    @Override
//    protected void onPostExecute(String result) {
//        if (result != null) {
//
//            int i = 0;
//
//            boolean endStationId = false;
//            int len = result.length();
//            for (i = 0; i < len && !endStationId ; i++){
//                String tmpStr = Character.toString(result.charAt(i));
//                if(tmpStr.equals("-")){
//                    endStationId = true;
//                }
//            }
//
//            String stationId = result.substring(0, i-2);
//            String placeId = result.substring(i, len-1);
//
//            SharedPreferences.Editor userSettingsEditor = userSettings.edit();
//            userSettingsEditor.putString(parentActivity.getString(R.string.USER_NFC_STATION_ID), stationId);
//            userSettingsEditor.putString(parentActivity.getString(R.string.USER_NFC_PLACE_ID), placeId);
//            userSettingsEditor.commit();
//
//            Toast.makeText(parentActivity, stationId + placeId,
//                    Toast.LENGTH_LONG).show();
//
//        }
//    }
//
//}
