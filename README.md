![Readme 배경](https://github.com/user-attachments/assets/b55711c7-d4b9-4c50-8010-5d3f0767ee06)

## 프로젝트 개요

미디어에 나온 유명 장소들을 공유할 수 있는 블로그 플랫폼입니다.   
백엔드 개발자 5명과 프론트엔드 개발자 1명이 약 4주간 진행했으며, 최우수 프로젝트에 선정되었습니다.

### 팀원 구성 및 역할

| 팀원                                  | 역할                                            |
|-------------------------------------|-----------------------------------------------|
| [김누리](https://github.com/NRKim93)   | 인프라 구축, 인증 및 인가                               |
| [류대현](https://github.com/jerrytrap) | 게시글 및 팔로우, 조회 수 처리                            |
| [이교원](https://github.com/LeeKyoWon) | 관리자 및 신고, ELK 로그 처리, ELK CI/CD, 인기 검색어/게시글 추출 |
| [한상훈](https://github.com/HSH02)     | 좋아요 기능, 임시 글 및 이미지 처리                         |
| [황윤구](https://github.com/hyg0527)   | 댓글 기능, 검색 및 검색어 자동 완성                         |

* * * 

## 주요 기능

- 전체 블로그 및 게시글 확인
- 위치 정보 및 해시태그를 추가해 게시글 작성
- 실시간 인기 글 및 검색어 조회
- '좋아요'한 포스트 모아보기
- 다른 사용자의 블로그 팔로우
- 게시글 댓글 작성
- 관리자 및 신고
- ELK 로그 처리

* * * 

## 기술 스택

| **카테고리**     | **기술**                                     |
|--------------|--------------------------------------------|
| **프로그래밍 언어** | **Java 17**                                |
| **프레임워크**    | **Spring Boot**                            |
| **데이터베이스**   | **MySQL, Redis**                           |
| **API 설계**   | **RESTful API**                            |
| **인증 & 보안**  | **JWT, OAuth2.0**                          |
| **배포 환경**    | **AWS(EC2, S3), NGINX, Docker, Terraform** |
| **로그**       | **ElasticSearch, Logstash, Kinaba, Beats** |
| **CI/CD**    | **GitHub Actions**                         |
| **테스트**      | **JUnit 5**                                |
| **문서화**      | **Springdoc, Swagger UI**                  |

* * * 

## 시스템 구조

![전체 구성도](https://github.com/user-attachments/assets/114827b9-643d-4905-bc2c-4f69370c629e)

* * * 

## CI/CD 구성도

![CI,CD](https://github.com/user-attachments/assets/b5a8500e-19ba-4d3c-b3c5-9072563f5e66)

* * * 

## ERD

![ERD](https://github.com/user-attachments/assets/4fe50a38-fedd-4069-ad7a-6460c29a2a40)
* * * 

## API 명세서

### [[Wiki]](https://github.com/prgrms-web-devcourse-final-project/WEB3_4_NDGL_BE/wiki)를 참고해주세요!

* * * 

## 실행 화면

### 메인 화면

![스크린샷 2025-04-18 181202](https://github.com/user-attachments/assets/ee414b7d-f638-4bfb-9d66-4ac5b3063195)
![스크린샷 2025-04-18 181145](https://github.com/user-attachments/assets/922e3b31-0a37-409e-8b14-f15f2db927c9)

### 임시 글 생성 화면

![임시저장](https://github.com/user-attachments/assets/31ab88c6-34a6-468e-8ec6-d901cdfa2df1)

### 글 생성 화면

![게시글 작성](https://github.com/user-attachments/assets/dff3dc43-6ae7-4211-a1dc-5db2f015f9c7)

### 글 상세 화면

![스크린샷 2025-04-18 오후 6 06 28](https://github.com/user-attachments/assets/c077ec7c-2866-483a-9171-7270241aab46)

### 내 게시글 화면

![image (1)](https://github.com/user-attachments/assets/3cba6115-8043-42a9-90d9-3ac4788a3929)

### 블로그 목록 화면

![스크린샷 2025-04-18 오후 6 16 50](https://github.com/user-attachments/assets/bfef66bc-5cbc-428e-8fc2-ef1dcebed8a1)

### 팔로우 목록 화면

![스크린샷 2025-04-18 오후 6 15 24](https://github.com/user-attachments/assets/e1447580-6b06-453b-8463-a2509df330a5)

### 검색 결과 화면

![스크린샷 2025-04-18 오후 6 11 39](https://github.com/user-attachments/assets/64fbd9a1-5501-4ff8-8b6d-c53bae0d133c)

* * * 

## 로그 관리

![로깅 파이프라인 - 일반](https://github.com/user-attachments/assets/567bb506-8e67-4db9-bbf9-ec1f3740f00b)

![로깅 파이프라인 - 검색,게시물 조회](https://github.com/user-attachments/assets/5b04b580-f999-40db-80b5-ba5b634ca9ee)

### 키바나 조회 로깅

![키바나 조회 로깅](https://github.com/user-attachments/assets/7ab8b640-2968-4811-be32-444d43180d1f)

### 키바나 검색 로깅

![키바나 검색 로깅](https://github.com/user-attachments/assets/8b67c80b-e948-4260-8618-44ffb833991c)

### 키바나 예외 로깅

![키바나 예외 로깅](https://github.com/user-attachments/assets/1cf9a462-0e81-4691-a932-c3c9a22102cf)

* * * 

## 디렉터리 구조

```
📦 src  
├── 📂 main  
│   ├── 📂 java/com/ndgl/spotfinder  
│   │   ├── 📂 domain                  
│   │   │   ├── 📂 admin               
│   │   │   ├── 📂 auth                
│   │   │   ├── 📂 blog                
│   │   │   ├── 📂 comment             
│   │   │   ├── 📂 follow              
│   │   │   ├── 📂 image               
│   │   │   ├── 📂 like                
│   │   │   ├── 📂 popular               
│   │   │   ├── 📂 post                
│   │   │   ├── 📂 report              
│   │   │   ├── 📂 search              
│   │   │   └── 📂 user                
│   │   │  
│   │   ├── 📂 global                  
│   │   │   ├── 📂 aspect              
│   │   │   │   └── 📂 logging         
│   │   │   ├── 📂 aws                 
│   │   │   │   └── 📂 s3              
│   │   │   ├── 📂 base                
│   │   │   ├── 📂 cache               
│   │   │   │   └── 📂 redis           
│   │   │   ├── 📂 common              
│   │   │   │   └── 📂 util            
│   │   │   ├── 📂 config              
│   │   │   │   ├── 📂 elastic          
│   │   │   │   └── 📂 redis           
│   │   │   ├── 📂 exception           
│   │   │   ├── 📂 initdata            
│   │   │   ├── 📂 rsdata              
│   │   │   └── 📂 security            
│   │   │       ├── 📂 cookie          
│   │   │       ├── 📂 jwt             
│   │   │       └── 📂 refresh         
│   │   │  
│   │   └── 📄 SpotfinderApplication.java
│   │  
│   └── 📂 resources  
│       ├── 📂 logs                     
│       ├── 📄 application.yml          
│       ├── 📄 console-appender.xml     
│       └── 📄 logback-spring.xml       
│  
├── 📂 test                             
│   └── 📂 java/com/ndgl/spotfinder  
│  
└── 📂 .github                          
    └── 📂 workflows                    
```

---

프론트엔드 코드는 링크를 참고해주세요! [[FE 링크]](https://github.com/prgrms-web-devcourse-final-project/WEB3_4_NDGL_FE)
