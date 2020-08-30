# JPA가 실무에서 어려운 이유
1. 객체와 테이블을 잘 설계하고 매핑하는게 중요하다.
2. 실무에서는 테이블이 많기 때문에 이를 적용하기 어렵다.
3. 실제로 적용했다가 매핑을 잘못하는 경우가 있다.

# JPA 내부 동작 방식 이해
1. JPA가 어떤 SQL을 만들어 내는가?
2. JPA가 언제 SQL을 실행하는가?
    * EntityManager->flush() 호출
    * EntityManager->commit() 호출 (flush 자동 호출)
    * JPQL 쿼리 실행 (flush 자동 호출)

# Persistence Context 이점
1. 1차 캐시
    * Entity Manager 내부에서 관리
    * EntityManager -> find 수행 시 1차 캐시에서 우선적으로 조회
        > 만약 캐시에 원하는 데이터가 없을 경우 DB 조회 후 캐시에 저장하고 이를 반환 
2. 동일성(identity) 보장
    * 동일한 트랙잭션에서 조회한 엔티티는 같음을 보장
3. 트랜잭션을 지원하는 쓰기 지연(transactional write-behind)
4. 변경 감지(Dirty Checking)
5. 지연 로딩(Lazy Loading)

# Persistence Context 관련 Method
* EntityManager->clear() : 영속성 컨텍스트 초기화

# 엔티티 매핑
1. 객체와 테이블 매핑: @Entity, @Table
    * 기본 생성자 필수(public or protected)
    * final 클래스, enum, interface, inner 클래스 사용 X
    * 저장할 필드에 final 사용 X
2. 연관관계 매핑: @ManyToOne, @JoinColumn 등

# 필드와 컬럼 매핑
| Annotation | Description | Detail |
|:--------|:--------|:--------|
| @Id | 기본 키 매핑 |  |
| @Column | 필드와 컬럼 매핑 | 아래 상세 기술 |
| @Enumerated | Enum 타입 매핑 | EnumType(ORDINAL, STRING), ORDINAL 사용 X |
| @Temporal | 시간/날짜 컬럼 매핑 | TemporalType(DATE, TIME, TIMESTAMP) |
| @Lob | LOB 매핑 | CLOB, BLOB |
| @Transient | 매핑 X |

# @Column Annotation
| 속성 | 설명 | 기본값 |
|:-----|:-----|:-----|
| name | 필드와 매핑할 테이블의 컬럼 이름 | 객체의 필드 이름
| insertable | 등록 가능 여부 | TRUE
| updatable | 변경 가능 여부 | TRUE
| nullable(DDL) | null 값의 허용 여부 설정. false로 설정하면 DDL 생성 시에 NOT NULL 제약 조건이 붙는다. | TRUE |
| unique(DDL) | @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용 | |
| columnDefinition(DDL) | 데이터베이스 컬럼 정보를 직접 줄 수 있다. <br/>ex) varchar(100) default 'EMPTY' | 필드의 자바 타입과 방언 정보를 사용 |
| length(DDL) | 문자 길이 제약조건, String 타입에만 사용 | 255
| precision, scale(DDL) | BigDecimal 타입에서 사용(BigInteger 에서도 사용 가능). precision은 소수점을 포함한 전체 자릿수를, scale은 소수의 자릿수. double, float 타입에는 적용되지 않는다. 아주 큰 숫자나 정밀한 소수를 다뤄야할 때만 사용. | precision=19,<br/>scale=2

# hibernate.hbm2ddl.auto 옵션
* create: 기존 테이블 삭제 후 다시 생성(DROP + CREATE)
* create-drop: create와 같으나 종료 시점에서 테이블 DROP
* update: 변경분만 반영(운영 DB에는 사용 X)
* validate: 엔티티와 테이블이 정상 매핑되었는지만 확인
* none: 사용하지 않음
> 운영 장비에는 절대 create, create-drop, update 사용하지 말자.

# hibernate.dialect 옵션에 따라 달라지는 SQL
* ORACLE, MySQL 등 선택한 DB에 따라 SQL이 유연하게 변화(EX. VARCHAR, VARCHAR2)

# DDL 생성 기능
* @Table Annotation은 JPA의 Runtime에 영향을 미친다.
* @Column Annotation의 unique, length, nullable 등은 JPA의 Runtime에 영향을 미치지 않는다.
