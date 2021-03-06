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
    > 단, @GeneratedValue 에서 IDENTITY 전략을 사용할 경우 persist 시점에서 SQL 실행 

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

# hibernate.dialect 옵션에 따라 달라지는 SQL
* ORACLE, MySQL 등 선택한 DB에 따라 SQL이 유연하게 변화(EX. VARCHAR, VARCHAR2)

# hibernate.hbm2ddl.auto 옵션
* create: 기존 테이블 삭제 후 다시 생성(DROP + CREATE)
* create-drop: create와 같으나 종료 시점에서 테이블 DROP
* update: 변경분만 반영(운영 DB에는 사용 X)
* validate: 엔티티와 테이블이 정상 매핑되었는지만 확인
* none: 사용하지 않음
> 운영 장비에는 절대 create, create-drop, update 사용하지 말자.

# DDL 생성 기능
* @Table Annotation은 JPA의 Runtime에 영향을 미친다.
* @Column Annotation의 unique, length, nullable 등은 JPA의 Runtime에 영향을 미치지 않는다.

# 기본키 매핑 방법
* 직접 할당 : @Id 사용
* @GenerateValue의 strategy
    - IDENTITY : 기본 키 생성을 데이터베이스에 위임()
    - SEQUENCE
    - TABLE
    - AUTO
    
# 양방향 연관관계 매핑시 주의점
* 단방향 매핑만으로 이미 연관관계 매핑은 완료
* 양방향을 지원하려면 연관관계 편의성 메서드 등으로 인해 오버헤드가 발생할 수 있다.
* 단방향 매핑을 잘 하고 양방향은 필요할 때 추가해도 됨(테이블에 영향 X)
    
# 연관관계의 주인을 정하는 기준
* 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안됨
* 연관관계의 주인은 외래 키의 위치를 기준으로 정해야함

# Inheritance 전략의 장단점
## JOINED
### 장점
1. 테이블 정규화
2. 외래 키 참조 무결성 제약조건 활용가능
3. 저장공간 효율화
### 단점
1. 조회시 조인을 많이 사용해 성능 저하
2. 조회 쿼리가 복잡함
3. 데이터 저장시 INSERT SQL 2번 호출

## SINGLE_TABLE
### 장점
1. 조인이 필요 없으므로 일반적으로 조회 성능이 빠름
2. 조회 쿼리가 단순함
### 단점
1. 자식 엔티티가 매핑한 컬럼은 모두 null 허용
2. 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있는 상황에 따라서 조회 성능이 오히려 느려질 수 있음

## TABLE_PER_CLASS
> DB 설계자와 ORM 전문가 모두 비선호
### 장점
1. 서브 타입을 명확하게 구분해서 처리할 때 효과적
2. not null 제약조건 사용 가능
### 단점
1. 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL)
2. 자식 테이블을 통합해서 쿼리하기 어려움

# @MappedSuperClass
## 개요
* 공통 매핑 정보가 필요할 때 사용(id, name 등)
* 상속관계 매핑(X)
* 엔티티(X), 테이블과 매핑(X)
* 부모 클래스를 상속 받는 자식 클래스에 매핑 정보만 제공
* 조회, 검색 불가(em.find(BaseEntity) 불가)
* 직접 생성해서 사용할 일 없으므로 추상 클래스로 선언하는걸 권장
## 정리
* 테이블과 관계 없고, 단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할
* 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통적으로 적용하는 정보를 모을 때 사용 
* 참고: @Entity 클래스는 @Entity / @MappedSuperClass 클래스만 상속 가능