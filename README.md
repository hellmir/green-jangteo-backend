# 🍉 친환경 마켓플레이스 프로젝트 그린장터 Server

## 📋 프로젝트 설명

- 제로베이스 First One 팀 프로젝트 그린장터 서버
- 온라인 상에서 친환경 물품들을 사고 팔 수 있는 마켓플레이스 서비스
<br>

## 📅 프로젝트 기간
<b>2023. 11. 20 ~ 2024. 01. 04</b>
<br><br>

## 👫 구성원

### 성효빈
- 팀장
- 회원, 가게, 주문, 쿠폰, 적립금, 채팅, 게시판, 댓글 기능 프로덕션, 테스트 코드 구현
- CI/CD 파이프라인 구축, 서버 배포 및 관리

### 김정훈
- 상품, 장바구니 기능 개발
<br>

## 📚 관련 URL

- [First One 팀 노션](https://elite-aletopelta-3ca.notion.site/1-FirstOne-d25c8b51a07643d98b349e7a64e70280?pvs=4)
- [그린장터 API 서버](https://green-jangteo.duckdns.org/swagger-ui/index.html)
- [그린장터 CI/CD 파이프라인](http://my-jenkins.duckdns.org:8080/job/green-jangteo)
- [서버 테스트 목록(419개)](https://ddipddipddip.s3.ap-northeast-2.amazonaws.com/test/greenjangteo/Test+results+-+Test+Summary.html)
- [Test Line Coverage(70%)](https://ddipddipddip.s3.ap-northeast-2.amazonaws.com/test/greenjangteo/green-jangteo.html)
<br>

## 🚀 배포

Vercel 컨테이너 Function으로 배포합니다(`vercel.json`, `vercel.Dockerfile`). 상품 검색은 `es` 프로파일에서만 켜지며, 기본 실행에서는 검색 API가 503을 반환합니다.

- **환경 변수:** `PGHOST_UNPOOLED`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`(Neon 연동), `KV_URL`(Upstash 연동), `JWT_SECRET_KEY`, `CRON_SECRET`, `FRONTEND_IP_FOR_GREEN_JANGTEO`, `FRONTEND_LOCAL_IP_FOR_SEUNGHAK`, `FRONTEND_LOCAL_IP_FOR_JIYOUNG`, `PORT`
- **스키마 생성(새 DB에 1회):** `vercel` 프로파일은 기동 시간을 줄이려고 스키마를 만들지 않습니다. 새 DB에는 위 환경 변수를 넣고 `SPRING_PROFILES_ACTIVE`를 `vercel`이 아닌 값으로 지정해 1회 실행합니다. 테이블, Spring Batch 메타 테이블, 기본 카테고리가 생성됩니다.
- **크론:** `vercel.json`의 `crons`가 매일 00시·06시(UTC)에 쿠폰 발급·만료 쿠폰 삭제를 호출합니다.
<br>

## 🗼 Architecture
![market-place drawio](https://github.com/user-attachments/assets/b63d7172-72bb-45cf-8e26-a19170a76585)
<br>

## 🗄️ ERD
![ERD](https://github.com/zerobase-first-one/green-jangteo-backend/assets/128391669/e1b4a49e-52da-4d88-b0b1-00b3e6aed9ac)
<br><br>

## 🛠️ Skills

## Back-End

- Java
- Spring Boot
- Spring Security
- Spring Batch
- Socket IO

## DevOps

- Jenkins
- Docker
- Vercel (Container Functions, Cron Jobs)
- PostgreSQL (Neon)
- Redis (Upstash)
- Elasticsearch (`es` 프로파일)
- H2
