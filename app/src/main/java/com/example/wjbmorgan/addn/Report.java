package com.example.wjbmorgan.addn;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/*
This class is the report activity which take the parameters from shared preference file,
request cloud servers for data, analyze the data, then display the result in pie chart
and bar chart.
 */

public class Report extends AppCompatActivity {

    public SharedPreferences preference;

    // The parameters that user input
    String gender;
    String insulin;
    String diabetes;
    int minAge;
    int maxAge;
    int minDuration;
    int maxDuration;
    boolean restriction;
    boolean type;
    String hba1cType;
    String hba1cUnit;
    int minHba1c;
    int maxHba1c;
    String reportType;

    // Part of URL for request
    String urlForVisit;
    String urlForPatient;

    // The result to be displayed in charts and sentences
    int[] genderParam;
    int[] genderHba1cParam;
    String[] genderMeanParam;
    int[] ageParam;
    int[] ageHba1cParam;
    String[] ageMeanParam;
    int[] insulinParam;
    int[] insulinHba1cParam;
    String[] insulinMeanParam;
    int[] durationParam;
    int[] durationHba1cParam;
    String[] durationMeanParam;
    int[] genderAgeParam;
    int[] insulinAgeParam;
    String[] meanParam;

    // The charts to be drawn
    PieChart pieChart;
    BarChart barChart;
    // The extra result to be shown
    TextView reportMessage;

    // The received data in a form of JSON array.
    JSONArray visitArray;
    JSONArray patientArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        preference = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the parameters in shared preference file
        gender = preference.getString("gender", " ").toUpperCase();
        insulin = preference.getString("insulin", " ");
        switch (insulin) {
            case "BD / Twice Daily":
                insulin = "BD_TWICE_DAILY";
                break;
            case "Other":
                insulin = "OTHER";
                break;
            case "Nill":
                insulin = "NILL";
                break;
        }
        diabetes = preference.getString("diabetes", " ");
        switch (diabetes) {
            case "Type 1":
                diabetes = "TYPE_1";
                break;
            case "Type 2":
                diabetes = "TYPE_2";
                break;
            case "Gestational":
                diabetes = "GESTATIONAL";
                break;
            case "Monogenic":
                diabetes = "MONOGENIC";
                break;
            case "CFRD":
                diabetes = "CFRD";
                break;
            case "Neonatal":
                diabetes = "NEONATAL";
                break;
            case "Unspecified":
                diabetes = "UNSPECIFIED";
                break;
            case "Other":
                diabetes = "OTHER";
                break;
        }
        minAge = preference.getInt("minAge", 0);
        maxAge = preference.getInt("maxAge", 100);
        minDuration = preference.getInt("minDuration", 0);
        maxDuration = preference.getInt("maxDuration", 20);
        restriction = preference.getBoolean("restriction", false);
        type = preference.getBoolean("hba1c_type", false);
        if (type) {
            hba1cType = "hba1c_ngsp";
            hba1cUnit = "(%)";
        } else {
            hba1cType = "hba1c_iffc";
            hba1cUnit = "(mmol/mol)";
        }
        minHba1c = preference.getInt("minHba1c", 0);
        maxHba1c = preference.getInt("maxHba1c", 100);
        reportType = preference.getString("report_type", " ");

        // Generate a URL to get the visit table
        urlForVisit = "http://130.56.252.66:3000/visit?order=local_id_id&select=local_id_id," + hba1cType + ",insulin_regimen&diagnosis_visit=eq.false&days_before_export=lte.365&hba1c_iffc=not.is.null";

        // Get all the ids of visit table
        String urlForId = "";
        visitThread.start();
        try {
            visitThread.join();
            urlForId = getId(visitArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String urlForDiabetes = "&diabetes_type_value=eq." + diabetes;

        // Generate the URL to get the patient table
        urlForPatient = "http://130.56.252.66:3000/patient?order=local_id_id&select=local_id_id,gender,age_at_export_in_days,diabetes_duration_in_days&active=eq.true" + urlForId + urlForDiabetes;
        if (restriction) {
            urlForPatient += "&consent_to_be_contacted=eq.true";
        }
        patientThread.start();
        try {
            patientThread.join();
            // Merge two arrays to get one array that contains all the needed information
            patientArray = merge(patientArray, visitArray);
            // From the JSON array generate all the parameters for the charts
            genderParam = genderParam(patientArray);
            genderHba1cParam = genderHba1cParam(patientArray);
            ageParam = ageParam(patientArray);
            ageHba1cParam = ageHba1cParam(patientArray);
            insulinParam = insulinParam(patientArray);
            insulinHba1cParam = insulinHba1cParam(patientArray);
            durationParam = durationParam(patientArray);
            durationHba1cParam = durationHba1cParam(patientArray);
            genderAgeParam = genderAgeParam(patientArray);
            insulinAgeParam = insulinAgeParam(patientArray);
            meanParam = meanParam(patientArray);
            genderMeanParam = genderMeanParam(patientArray);
            ageMeanParam = ageMeanParam(patientArray);
            insulinMeanParam = insulinMeanParam(patientArray);
            durationMeanParam = durationMeanParam(patientArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pieChart = (PieChart) findViewById(R.id.pie_chart);
        barChart = (BarChart) findViewById(R.id.bar_chart);
        reportMessage = (TextView) findViewById(R.id.report_message);
        // According to the report type that user chose, draw a pie chart, a bar chart and a description
        // for extra result.
        switch (reportType) {
            case ("Gender Distribution"):
                String[] genderLabel = {"Male", "Female", "Undetermined"};
                drawPieChart(pieChart, "Gender Distribution", genderParam, genderLabel);
                drawBarChart(barChart, "Gender Distribution", genderParam, genderLabel);
                reportMessage.setText("Mean age (years) is " + meanParam[0] + ".\n" + "Mean duration (years) is " + meanParam[1] + ".\n" +
                        "Mean Hba1c " + hba1cUnit + " is " + meanParam[2] + ".\n" + "HbA1c range " + hba1cUnit + " is " + meanParam[3] + " ~ " + meanParam[4] + ".");
                break;
            case ("HbA1c Distribution of Selected Gender"):
                String[] genderHLabel = {"~" + minHba1c, minHba1c + "~" + maxHba1c, maxHba1c + "~"};
                drawPieChart(pieChart, "HbA1c Distribution of Selected Gender", genderHba1cParam, genderHLabel);
                drawBarChart(barChart, "HbA1c Distribution of Selected Gender", genderHba1cParam, genderHLabel);
                reportMessage.setText("Mean age (years) of selected gender is " + genderMeanParam[0] + ".\n" + "Mean duration (years) of selected gender is " + genderMeanParam[1] + ".\n" +
                        "Mean Hba1c of selected gender " + hba1cUnit + " is " + genderMeanParam[2] + ".\n" + "HbA1c range of selected gender " + hba1cUnit + " is " + genderMeanParam[3] + " ~ " + genderMeanParam[4] + ".");
                break;
            case ("Age Distribution"):
                String[] ageLabel = {"~" + minAge, minAge + "~" + maxAge, maxAge + "~"};
                drawPieChart(pieChart, "Age Distribution", ageParam, ageLabel);
                drawBarChart(barChart, "Age Distribution", ageParam, ageLabel);
                reportMessage.setText("Mean age (years) is " + meanParam[0] + ".\n" + "Mean duration (years) is " + meanParam[1] + ".\n" +
                        "Mean Hba1c " + hba1cUnit + " is " + meanParam[2] + ".\n" + "HbA1c range " + hba1cUnit + " is " + meanParam[3] + " ~ " + meanParam[4] + ".");
                break;
            case ("HbA1c Distribution of Selected Age Range"):
                String[] ageHLabel = {"~" + minHba1c, minHba1c + "~" + maxHba1c, maxHba1c + "~"};
                drawPieChart(pieChart, "HbA1c Distribution of Selected Age Range", ageHba1cParam, ageHLabel);
                drawBarChart(barChart, "HbA1c Distribution of Selected Age Range", ageHba1cParam, ageHLabel);
                reportMessage.setText("Mean age (years) of selected age range is " + ageMeanParam[0] + ".\n" + "Mean duration (years) of selected age range is " + ageMeanParam[1] + ".\n" +
                        "Mean Hba1c of selected age range " + hba1cUnit + " is " + ageMeanParam[2] + ".\n" + "HbA1c range of selected age range " + hba1cUnit + " is " + ageMeanParam[3] + " ~ " + ageMeanParam[4] + ".");
                break;
            case ("Insulin Regimens Distribution"):
                String[] insulinLabel = {"CSII", "BD / Twice Daily", "MDI", "Other", "Nill"};
                drawPieChart(pieChart, "Insulin Regimens Distribution", insulinParam, insulinLabel);
                drawBarChart(barChart, "Insulin Regimens Distribution", insulinParam, insulinLabel);
                reportMessage.setText("Mean age (years) is " + meanParam[0] + ".\n" + "Mean duration (years) is " + meanParam[1] + ".\n" +
                        "Mean Hba1c " + hba1cUnit + " is " + meanParam[2] + ".\n" + "HbA1c range " + hba1cUnit + " is " + meanParam[3] + " ~ " + meanParam[4] + ".");
                break;
            case ("HbA1c Distribution of Selected Insulin Regimen"):
                String[] insulinHLabel = {"~" + minHba1c, minHba1c + "~" + maxHba1c, maxHba1c + "~"};
                drawPieChart(pieChart, "HbA1c Distribution of Selected Insulin Regimen", ageHba1cParam, insulinHLabel);
                drawBarChart(barChart, "HbA1c Distribution of Selected Insulin Regimen", ageHba1cParam, insulinHLabel);
                reportMessage.setText("Mean age (years) of selected insulin regimen is " + insulinMeanParam[0] + ".\n" + "Mean duration (years) of selected insulin regimen is " + insulinMeanParam[1] + ".\n" +
                        "Mean Hba1c of selected insulin regimen " + hba1cUnit + " is " + insulinMeanParam[2] + ".\n" + "HbA1c range of selected insulin regimen " + hba1cUnit + " is " + insulinMeanParam[3] + " ~ " + insulinMeanParam[4] + ".");
                break;
            case ("Diabetes Duration Distribution"):
                String[] durationLabel = {"~" + minDuration, minDuration + "~" + maxDuration, maxDuration + "~"};
                drawPieChart(pieChart, "Diabetes Duration Distribution", durationParam, durationLabel);
                drawBarChart(barChart, "Diabetes Duration Distribution", durationParam, durationLabel);
                reportMessage.setText("Mean age (years) is " + meanParam[0] + ".\n" + "Mean duration (years) is " + meanParam[1] + ".\n" +
                        "Mean Hba1c " + hba1cUnit + " is " + meanParam[2] + ".\n" + "HbA1c range " + hba1cUnit + " is " + meanParam[3] + " ~ " + meanParam[4] + ".");
                break;
            case ("HbA1c Distribution of Selected Diabetes Duration"):
                String[] durationHLabel = {"~" + minHba1c, minHba1c + "~" + maxHba1c, maxHba1c + "~"};
                drawPieChart(pieChart, "HbA1c Distribution of Selected Diabetes Duration", durationHba1cParam, durationHLabel);
                drawBarChart(barChart, "HbA1c Distribution of Selected Diabetes Duration", durationHba1cParam, durationHLabel);
                reportMessage.setText("Mean age (years) of selected diabetes duration is " + durationMeanParam[0] + ".\n" + "Mean duration (years) of selected diabetes duration is " + durationMeanParam[1] + ".\n" +
                        "Mean Hba1c of selected diabetes duration " + hba1cUnit + " is " + durationMeanParam[2] + ".\n" + "HbA1c range of selected diabetes duration " + hba1cUnit + " is " + durationMeanParam[3] + " ~ " + durationMeanParam[4] + ".");
                break;
            case ("Age Range Breakdown by Selected Gender"):
                String[] genderALabel = {"~" + minAge, minAge + "~" + maxAge, maxAge + "~"};
                drawPieChart(pieChart, "Age Range Breakdown by Selected Gender", genderAgeParam, genderALabel);
                drawBarChart(barChart, "Age Range Breakdown by Selected Gender", genderAgeParam, genderALabel);
                break;
            case ("Insulin Regimen Breakdown by Age Range"):
                String[] insulinALabel = {"~" + minAge, minAge + "~" + maxAge, maxAge + "~"};
                drawPieChart(pieChart, "Insulin Regimen Breakdown by Age Range", insulinAgeParam, insulinALabel);
                drawBarChart(barChart, "Insulin Regimen Breakdown by Age Range", insulinAgeParam, insulinALabel);
                break;
        }

    }

    // This method is used to draw a pie chart using parameters generated from the JSON array.
    public void drawPieChart(PieChart pieChart, String description, int[] values, String[] labels) {
        Description d = new Description();
        d.setTextSize(12f);
        d.setText(description);
        pieChart.setDescription(d);
        List<PieEntry> entry = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            entry.add(new PieEntry(values[i], labels[i]));
        }
        PieDataSet dataSet = new PieDataSet(entry, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHoleRadius(40f);
        pieChart.invalidate();
    }

    // This method is used to draw a bar chart using parameters generated from the JSON array.
    public void drawBarChart(BarChart barChart, String description, int[] values, String[] labels) {
        Description d = new Description();
        d.setTextSize(12f);
        d.setText(description);
        barChart.setDescription(d);
        List<BarEntry> entry = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            entry.add(new BarEntry(i, values[i]));
        }
        BarDataSet dataSet = new BarDataSet(entry, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.getXAxis().setValueFormatter(new LabelFormatter(labels));
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setGranularity(1f);
        barChart.invalidate();
    }

    // This class is for display the labels of bar charts
    public class LabelFormatter implements IAxisValueFormatter {
        private final String[] mLabels;

        LabelFormatter(String[] labels) {
            mLabels = labels;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mLabels[(int) value];
        }
    }

    // Following methods are used to Generate parameters from JSON array.
    public int[] genderParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("gender").equals("MALE")) {
                a++;
            } else if (jsonArray.getJSONObject(i).getString("gender").equals("FEMALE")) {
                b++;
            } else {
                c++;
            }
        }
        return new int[]{a, b, c};
    }

    public int[] genderHba1cParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("gender").equals(gender)) {
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) < minHba1c) {
                    a++;
                } else if (jsonArray.getJSONObject(i).getDouble(hba1cType) >= maxHba1c) {
                    c++;
                } else {
                    b++;
                }
            }
        }
        return new int[]{a, b, c};
    }

    public int[] ageParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 < minAge) {
                a++;
            } else if (jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 >= maxAge) {
                c++;
            } else {
                b++;
            }
        }
        return new int[]{a, b, c};
    }

    public int[] ageHba1cParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 >= minAge &&
                    jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 < maxAge) {
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) < minHba1c) {
                    a++;
                } else if (jsonArray.getJSONObject(i).getDouble(hba1cType) >= maxHba1c) {
                    c++;
                } else {
                    b++;
                }
            }
        }
        return new int[]{a, b, c};
    }

    public int[] insulinParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;
        int e = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("insulin_regimen").equals("CSII")) {
                a++;
            } else if (jsonArray.getJSONObject(i).getString("insulin_regimen").equals("BD_TWICE_DAILY")) {
                b++;
            } else if (jsonArray.getJSONObject(i).getString("insulin_regimen").equals("MDI")) {
                c++;
            } else if (jsonArray.getJSONObject(i).getString("insulin_regimen").equals("OTHER")) {
                d++;
            } else if (jsonArray.getJSONObject(i).getString("insulin_regimen").equals("NILL")) {
                e++;
            }
        }
        return new int[]{a, b, c, d, e};
    }

    public int[] insulinHba1cParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("insulin_regimen").equals(insulin)) {
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) < minHba1c) {
                    a++;
                } else if (jsonArray.getJSONObject(i).getDouble(hba1cType) >= maxHba1c) {
                    c++;
                } else {
                    b++;
                }
            }
        }
        return new int[]{a, b, c};
    }

    public int[] durationParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days") / 365 < minDuration) {
                a++;
            } else if (jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days") / 365 >= maxDuration) {
                c++;
            } else {
                b++;
            }
        }
        return new int[]{a, b, c};
    }

    public int[] durationHba1cParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days") / 365 >= minDuration &&
                    jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days") / 365 < maxDuration) {
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) < minHba1c) {
                    a++;
                } else if (jsonArray.getJSONObject(i).getDouble(hba1cType) >= maxHba1c) {
                    c++;
                } else {
                    b++;
                }
            }
        }
        return new int[]{a, b, c};
    }

    public int[] genderAgeParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("gender").equals(gender)) {
                if (jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 < minAge) {
                    a++;
                } else if (jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 >= maxAge) {
                    c++;
                } else {
                    b++;
                }
            }
        }
        return new int[]{a, b, c};
    }

    public int[] insulinAgeParam(JSONArray jsonArray) throws JSONException {
        int a = 0;
        int b = 0;
        int c = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("insulin_regimen").equals(insulin)) {
                if (jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 < minAge) {
                    a++;
                } else if (jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 >= maxAge) {
                    c++;
                } else {
                    b++;
                }
            }
        }
        return new int[]{a, b, c};
    }

    public String[] meanParam(JSONArray jsonArray) throws JSONException {
        Double a = 0.0;
        Double b = 0.0;
        Double c = 0.0;
        Double minH = 150.0;
        Double maxH = 0.0;
        for (int i = 0; i < jsonArray.length(); i++) {
            a += jsonArray.getJSONObject(i).getInt("age_at_export_in_days");
            b += jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days");
            c += jsonArray.getJSONObject(i).getDouble(hba1cType);
            if (jsonArray.getJSONObject(i).getDouble(hba1cType) < minH) {
                minH = jsonArray.getJSONObject(i).getDouble(hba1cType);
            }
            if (jsonArray.getJSONObject(i).getDouble(hba1cType) > maxH) {
                maxH = jsonArray.getJSONObject(i).getDouble(hba1cType);
            }
        }
        String meanAge = new DecimalFormat("0.0").format((a / 365) / jsonArray.length());
        String meanDuration = new DecimalFormat("0.0").format((b / 365) / jsonArray.length());
        String meanHba1c = new DecimalFormat("0.0").format(c / jsonArray.length());

        return new String[]{meanAge, meanDuration, meanHba1c, minH.toString(), maxH.toString()};
    }

    public String[] genderMeanParam(JSONArray jsonArray) throws JSONException {
        Double a = 0.0;
        Double b = 0.0;
        Double c = 0.0;
        int d = 0;
        Double minH = 150.0;
        Double maxH = 0.0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("gender").equals(gender)) {
                a += jsonArray.getJSONObject(i).getInt("age_at_export_in_days");
                b += jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days");
                c += jsonArray.getJSONObject(i).getDouble(hba1cType);
                d++;
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) < minH) {
                    minH = jsonArray.getJSONObject(i).getDouble(hba1cType);
                }
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) > maxH) {
                    maxH = jsonArray.getJSONObject(i).getDouble(hba1cType);
                }
            }
        }
        String meanAge = new DecimalFormat("0.0").format((a / 365) / d);
        String meanDuration = new DecimalFormat("0.0").format((b / 365) / d);
        String meanHba1c = new DecimalFormat("0.0").format(c / d);

        return new String[]{meanAge, meanDuration, meanHba1c, minH.toString(), maxH.toString()};
    }

    public String[] ageMeanParam(JSONArray jsonArray) throws JSONException {
        Double a = 0.0;
        Double b = 0.0;
        Double c = 0.0;
        int d = 0;
        Double minH = 150.0;
        Double maxH = 0.0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 >= minAge &&
                    jsonArray.getJSONObject(i).getInt("age_at_export_in_days") / 365 < maxAge) {
                a += jsonArray.getJSONObject(i).getInt("age_at_export_in_days");
                b += jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days");
                c += jsonArray.getJSONObject(i).getDouble(hba1cType);
                d++;
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) < minH) {
                    minH = jsonArray.getJSONObject(i).getDouble(hba1cType);
                }
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) > maxH) {
                    maxH = jsonArray.getJSONObject(i).getDouble(hba1cType);
                }
            }
        }
        String meanAge = new DecimalFormat("0.0").format((a / 365) / d);
        String meanDuration = new DecimalFormat("0.0").format((b / 365) / d);
        String meanHba1c = new DecimalFormat("0.0").format(c / d);

        return new String[]{meanAge, meanDuration, meanHba1c, minH.toString(), maxH.toString()};
    }

    public String[] insulinMeanParam(JSONArray jsonArray) throws JSONException {
        Double a = 0.0;
        Double b = 0.0;
        Double c = 0.0;
        int d = 0;
        Double minH = 150.0;
        Double maxH = 0.0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getString("insulin_regimen").equals(insulin)) {
                a += jsonArray.getJSONObject(i).getInt("age_at_export_in_days");
                b += jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days");
                c += jsonArray.getJSONObject(i).getDouble(hba1cType);
                d++;
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) < minH) {
                    minH = jsonArray.getJSONObject(i).getDouble(hba1cType);
                }
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) > maxH) {
                    maxH = jsonArray.getJSONObject(i).getDouble(hba1cType);
                }
            }
        }
        String meanAge = new DecimalFormat("0.0").format((a / 365) / d);
        String meanDuration = new DecimalFormat("0.0").format((b / 365) / d);
        String meanHba1c = new DecimalFormat("0.0").format(c / d);

        return new String[]{meanAge, meanDuration, meanHba1c, minH.toString(), maxH.toString()};
    }

    public String[] durationMeanParam(JSONArray jsonArray) throws JSONException {
        Double a = 0.0;
        Double b = 0.0;
        Double c = 0.0;
        int d = 0;
        Double minH = 150.0;
        Double maxH = 0.0;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days") / 365 >= minDuration &&
                    jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days") / 365 < maxDuration) {
                a += jsonArray.getJSONObject(i).getInt("age_at_export_in_days");
                b += jsonArray.getJSONObject(i).getInt("diabetes_duration_in_days");
                c += jsonArray.getJSONObject(i).getDouble(hba1cType);
                d++;
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) < minH) {
                    minH = jsonArray.getJSONObject(i).getDouble(hba1cType);
                }
                if (jsonArray.getJSONObject(i).getDouble(hba1cType) > maxH) {
                    maxH = jsonArray.getJSONObject(i).getDouble(hba1cType);
                }
            }
        }
        String meanAge = new DecimalFormat("0.0").format((a / 365) / d);
        String meanDuration = new DecimalFormat("0.0").format((b / 365) / d);
        String meanHba1c = new DecimalFormat("0.0").format(c / d);

        return new String[]{meanAge, meanDuration, meanHba1c, minH.toString(), maxH.toString()};
    }

    // Merge two JSON array to be one.
    public JSONArray merge(JSONArray patient, JSONArray visit) throws JSONException {
        for (int i = 0; i < patient.length(); i++) {
            for (int j = 0; j < visit.length(); j++) {
                if (patient.getJSONObject(i).getInt("local_id_id") == visit.getJSONObject(j).getInt("local_id_id")) {
                    patient.getJSONObject(i).put(hba1cType, visit.getJSONObject(j).getDouble(hba1cType));
                    patient.getJSONObject(i).put("insulin_regimen", visit.getJSONObject(j).getString("insulin_regimen"));
                    break;
                }
            }
        }
        return patient;
    }

    // This method is used to get all the ids form a JSON array
    public String getId(JSONArray jsonArray) throws JSONException {
        String result = "&local_id_id=in.";
        for (int i = 0; i < jsonArray.length(); i++) {
            result = result + jsonArray.getJSONObject(i).getInt("local_id_id") + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    // Request for visit table from the server.
    Thread visitThread = new Thread(new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            StringBuilder response = new StringBuilder("");
            String result = null;
            try {
                URL url = new URL(urlForVisit);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-length", "0");
                connection.setUseCaches(false);
                connection.setAllowUserInteraction(false);
                connection.connect();
                int status = connection.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = rd.readLine()) != null) {
                            response.append(line);
                        }
                        rd.close();
                        result = response.toString();
                }
                visitArray = new JSONArray(result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    // Request for patient table from the server.
    Thread patientThread = new Thread(new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            StringBuilder response = new StringBuilder("");
            String result = null;
            try {
                URL url = new URL(urlForPatient);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-length", "0");
                connection.setUseCaches(false);
                connection.setAllowUserInteraction(false);
                connection.connect();
                int status = connection.getResponseCode();
                switch (status) {
                    case 200:
                    case 201:
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = rd.readLine()) != null) {
                            response.append(line);
                        }
                        rd.close();
                        result = response.toString();
                }
                patientArray = new JSONArray(result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

}
