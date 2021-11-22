# RestTemplate을 이용한 mTLS 통신 예제

## 선행작업
mTLS 인증서정보를 이용하여 java keyStore를 생성한다.  
keyStore 생성하는 방법은 다음 [링크](https://www.lesstif.com/java/java-keytool-keystore-20775436.html)를 참고 한다.  
  

## App.java
RestTemplate을 이용하여 mTLS 통신을 하기 위해 java keyStore에 있는 정보를 읽어들여와 SSLContext를 생성한다.  
그리고 Proxy 서버를 통해서 대내외 통신이 이뤄질 수 있기 때문에 Proxy 설정도 추가하였다.  