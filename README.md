

# AutoHCS
JAVA 기반의 안드로이드 건강상태자가진단 설문 모듈

## 필요한 라이브러리
```txt
https://github.com/google/volley
```

## 사용방법
```txt
  public void encryptProfile(...){ ... ~  private void registerSurvey(...){ ... } 부분까지 사용해야합니다.
  
  
  encryptProfile 함수의 파라미터
  - name / String : 평문의 문자열
  - birth / String : 평문의 6자리 숫자
  - password / String : 평문의 4자리 숫자
  - orgCode / String : 학교 코드
  - region / String : 지역 코드
  
  EX) 이름: 홍길동, 생년월일: 040201, 비밀번호: 1234, 학교:한국고등학교-12345(해당 학교의 코드를 사용해야함) ,지역: 울산-use(해당 지역의 코드를 사용해야함)
  encryptProfile("홍길동", "040201","1234", "12345", "use");
  로 설문을 완료할 수 있습니다.(이었던것...)
```

## DEPRECATED
```txt
자가진단 사이트 로그인시 보안 키패드가 적용됨에 따라 이 소스코드를 그대로 사용할 수 없습니다!
보안 키패드 모듈을 작성해야 사용이 가능합니다.
```
