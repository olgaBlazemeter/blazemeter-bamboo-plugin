package com.blazemeter.bamboo.plugin.testresult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * BlazeMeter
 * User: moshe
 * Date: 5/28/12
 * Time: 12:51 PM
 */
public class TestResultV2Impl extends TestResult {

    TestResultV2Impl(JSONObject json) throws IOException, JSONException {

        this.std = json.getDouble("std");
        this.average = json.getDouble("average");
        this.min = json.getDouble("min");
        this.max = json.getDouble("max");
        this.samples = json.getDouble("samples");
        this.median = json.getDouble("median");
        this.percentile90 = json.getDouble("percentile90");
        this.errorPercentage = json.getDouble("errorPercentage");
        this.hits = json.getDouble("hits");
        this.kbs = json.getDouble("kbs");
        this.n = json.getLong("n");

    }

    @Override
    public String toString() {
        return "####### Aggregate Test Result ########" +
                "\n Average=" + average +
                ",\n Min=" + min +
                ",\n Max=" + max +
                ",\n ErrorPercentage=" + errorPercentage;
    }

    public double getStd() {
        return std;
    }

    public void setStd(double std) {
        this.std = std;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getSamples() {
        return samples;
    }

    public void setSamples(double samples) {
        this.samples = samples;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getPercentile90() {
        return percentile90;
    }

    public void setPercentile90(double percentile90) {
        this.percentile90 = percentile90;
    }

    public double getPercentile99() {
        return percentile99;
    }

    public void setPercentile99(double percentile99) {
        this.percentile99 = percentile99;
    }

    public double getErrorPercentage() {
        return errorPercentage;
    }

    public void setErrorPercentage(double errorPercentage) {
        this.errorPercentage = errorPercentage;
    }

    public double getHits() {
        return hits;
    }

    public void setHits(double hits) {
        this.hits = hits;
    }

    public double getKbs() {
        return kbs;
    }

    public void setKbs(double kbs) {
        this.kbs = kbs;
    }

    public long getN() {
        return n;
    }

    public void setN(long n) {
        this.n = n;
    }

}