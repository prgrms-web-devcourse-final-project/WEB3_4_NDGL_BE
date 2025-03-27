package com.ndgl.spotfinder.global.aws.s3;

import java.util.UUID;


public class S3Util {
    private S3Util(){}

    // 사진 저장 path 설정
    public static String buildS3Key(long id, String fileType) {
        return  UUID.randomUUID().toString();
    }

    public static String extractObjectKeyFromUrl(String url) {
        int domainEndIndex = url.indexOf(".com/");
        if (domainEndIndex != -1) {
            return url.substring(domainEndIndex + 5); // ".com/" 의 길이인 5를 더해줍니다
        }
        return null;
    }

}
