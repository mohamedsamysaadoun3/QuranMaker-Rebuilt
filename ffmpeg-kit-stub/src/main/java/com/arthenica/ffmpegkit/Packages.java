package com.arthenica.ffmpegkit;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class Packages {
    private static final List<String> supportedExternalLibraries;

    static {
        ArrayList arrayList = new ArrayList();
        supportedExternalLibraries = arrayList;
        arrayList.add("dav1d");
        arrayList.add("fontconfig");
        arrayList.add("freetype");
        arrayList.add("fribidi");
        arrayList.add("gmp");
        arrayList.add("gnutls");
        arrayList.add("kvazaar");
        arrayList.add("mp3lame");
        arrayList.add("libass");
        arrayList.add("iconv");
        arrayList.add("libilbc");
        arrayList.add("libtheora");
        arrayList.add("libvidstab");
        arrayList.add("libvorbis");
        arrayList.add("libvpx");
        arrayList.add("libwebp");
        arrayList.add("libxml2");
        arrayList.add("opencore-amr");
        arrayList.add("openh264");
        arrayList.add("openssl");
        arrayList.add("opus");
        arrayList.add("rubberband");
        arrayList.add("sdl2");
        arrayList.add("shine");
        arrayList.add("snappy");
        arrayList.add("soxr");
        arrayList.add("speex");
        arrayList.add("srt");
        arrayList.add("tesseract");
        arrayList.add("twolame");
        arrayList.add("x264");
        arrayList.add("x265");
        arrayList.add("xvid");
        arrayList.add("zimg");
    }

    public static String getPackageName() {
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        boolean z5;
        List<String> externalLibraries = getExternalLibraries();
        boolean contains = externalLibraries.contains("speex");
        boolean contains2 = externalLibraries.contains("fribidi");
        boolean contains3 = externalLibraries.contains("gnutls");
        boolean contains4 = externalLibraries.contains("xvid");
        boolean z6 = true;
        boolean z7 = false;
        if (!contains || !contains2) {
            if (contains) {
                z2 = true;
                z = false;
                z4 = false;
            } else {
                if (contains2) {
                    z = true;
                    z2 = false;
                } else if (contains4) {
                    if (contains3) {
                        z4 = true;
                        z = false;
                        z2 = false;
                        z3 = false;
                        z6 = z3;
                        z5 = z6;
                    } else {
                        z5 = true;
                        z = false;
                        z2 = false;
                        z4 = false;
                        z3 = false;
                        z6 = false;
                    }
                } else if (contains3) {
                    z3 = true;
                    z = false;
                    z2 = false;
                    z4 = false;
                    z6 = false;
                    z5 = z6;
                } else {
                    z = false;
                    z2 = false;
                }
                z4 = z2;
            }
            z3 = z4;
            z6 = z3;
            z5 = z6;
        } else if (contains4) {
            z = false;
            z2 = false;
            z4 = false;
            z3 = false;
            z5 = false;
        } else {
            z = false;
            z2 = false;
            z4 = false;
            z3 = false;
            z5 = false;
            z7 = true;
            z6 = false;
        }
        return z6 ? (externalLibraries.contains("dav1d") && externalLibraries.contains("fontconfig") && externalLibraries.contains("freetype") && externalLibraries.contains("fribidi") && externalLibraries.contains("gmp") && externalLibraries.contains("gnutls") && externalLibraries.contains("kvazaar") && externalLibraries.contains("mp3lame") && externalLibraries.contains("libass") && externalLibraries.contains("iconv") && externalLibraries.contains("libilbc") && externalLibraries.contains("libtheora") && externalLibraries.contains("libvidstab") && externalLibraries.contains("libvorbis") && externalLibraries.contains("libvpx") && externalLibraries.contains("libwebp") && externalLibraries.contains("libxml2") && externalLibraries.contains("opencore-amr") && externalLibraries.contains("opus") && externalLibraries.contains("shine") && externalLibraries.contains("snappy") && externalLibraries.contains("soxr") && externalLibraries.contains("speex") && externalLibraries.contains("twolame") && externalLibraries.contains("x264") && externalLibraries.contains("x265") && externalLibraries.contains("xvid") && externalLibraries.contains("zimg")) ? "full-gpl" : "custom" : z7 ? (externalLibraries.contains("dav1d") && externalLibraries.contains("fontconfig") && externalLibraries.contains("freetype") && externalLibraries.contains("fribidi") && externalLibraries.contains("gmp") && externalLibraries.contains("gnutls") && externalLibraries.contains("kvazaar") && externalLibraries.contains("mp3lame") && externalLibraries.contains("libass") && externalLibraries.contains("iconv") && externalLibraries.contains("libilbc") && externalLibraries.contains("libtheora") && externalLibraries.contains("libvorbis") && externalLibraries.contains("libvpx") && externalLibraries.contains("libwebp") && externalLibraries.contains("libxml2") && externalLibraries.contains("opencore-amr") && externalLibraries.contains("opus") && externalLibraries.contains("shine") && externalLibraries.contains("snappy") && externalLibraries.contains("soxr") && externalLibraries.contains("speex") && externalLibraries.contains("twolame") && externalLibraries.contains("zimg")) ? "full" : "custom" : z ? (externalLibraries.contains("dav1d") && externalLibraries.contains("fontconfig") && externalLibraries.contains("freetype") && externalLibraries.contains("fribidi") && externalLibraries.contains("kvazaar") && externalLibraries.contains("libass") && externalLibraries.contains("iconv") && externalLibraries.contains("libtheora") && externalLibraries.contains("libvpx") && externalLibraries.contains("libwebp") && externalLibraries.contains("snappy") && externalLibraries.contains("zimg")) ? "video" : "custom" : z2 ? (externalLibraries.contains("mp3lame") && externalLibraries.contains("libilbc") && externalLibraries.contains("libvorbis") && externalLibraries.contains("opencore-amr") && externalLibraries.contains("opus") && externalLibraries.contains("shine") && externalLibraries.contains("soxr") && externalLibraries.contains("speex") && externalLibraries.contains("twolame")) ? "audio" : "custom" : z4 ? (externalLibraries.contains("gmp") && externalLibraries.contains("gnutls") && externalLibraries.contains("libvidstab") && externalLibraries.contains("x264") && externalLibraries.contains("x265") && externalLibraries.contains("xvid")) ? "https-gpl" : "custom" : z3 ? (externalLibraries.contains("gmp") && externalLibraries.contains("gnutls")) ? "https" : "custom" : z5 ? (externalLibraries.contains("libvidstab") && externalLibraries.contains("x264") && externalLibraries.contains("x265") && externalLibraries.contains("xvid")) ? "min-gpl" : "custom" : externalLibraries.size() == 0 ? "min" : "custom";
    }

    public static List<String> getExternalLibraries() {
        String nativeBuildConf = AbiDetect.getNativeBuildConf();
        ArrayList arrayList = new ArrayList();
        for (String str : supportedExternalLibraries) {
            if (nativeBuildConf.contains("enable-" + str) || nativeBuildConf.contains("enable-lib" + str)) {
                arrayList.add(str);
            }
        }
        Collections.sort(arrayList);
        return arrayList;
    }
}
