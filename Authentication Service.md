---
title: Authentication Service

---

# Authentication Service
## Index
1. [Requests](##Requestes)

2. [APIs](##APIs)
    - [Registration](###Register)
    - [Request activation code](###Request_activation_code)
    - [Activation](###Activate)
    - [Login (get token)](###Get_token_(Login))
    - [Logout](###Logout)
    - [Token validation](###Token_Validation)
    - [Group Check](###Group_Check)
    - [Password recovery](###Request_password_recovery)
    - [Verify password recovery](###Verify_password_recovery)
    - [Change password](###Change_password)



## Requests
This module was made to implement the following features.
- User sign up with activation througth email.
- [Login](###Get_token_(Login)) and token issuing.
- Password recovery
- Token interpretation library

## APIs
response.addHeader("Authorization", theToken)
### Register
#### Endpoint
- Type `POST`
- `/token/register`
#### Inputs
```json
{
    email: "Email"
    password: "password"
    firstName: "firstName"
    lastName: "lastName"
}
```
#### Returns

##### Valid
- A 200 HTTP response could mean that the call was successful or that the email was invalid (We will not reveal it with an error code to keep the information hidden from bad actors).

##### Invalid
- 401 HTTP response code.
- A 400 HTTP error if the IP has been rate limited
### Request_activation_code
#### Endpoint
- Type `POST`
- `/activate`
#### Inputs
```json
{
    "recepient":"email",
}
```
#### Returns
##### Valid

- When called this endpoint will send a message to the recipient using the specified channel ( if implemented ).
##### Invalid
- A 400 HTTP response code if the IP has been rate limited.
- A 200 HTTP response code could mean that the call was successful or that the email was invalid (We will not reveal it with an error code to keep the information hidden from bad actors).

### Activate
#### Endpoint
- Type `POST`
- `/activate/authenticate`
#### Inputs
```json
{
    email: "Email"
    OTP: "Code"
}
```
#### Returns

##### Valid
```json
{
    JWT_Token: "Token"
}
```

You can set it in the header of a request using the following snippets.
```java
//java
HttpRequest request = HttpRequest.newBuilder()
    .header("Authorization", "bearer " + token)
```
```javascript
//js with axios

axios.defaults.headers.common['Authorization'] = `bearer <token>`
```
```javascript
//js without axios
fetch('https://api.example.com/protected', {
  headers: {
    'Authorization': `bearer <token>`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));   
```
```javascript
const headers = new HttpHeaders({
  'Authorization': 'bearer my-token',
});

this.http.get('https://api.npms.io/v2/search?q=scope:angular', { headers }).subscribe(data => {
  this.totalAngularPackages = data.total;
});
```

##### Invalid

- 401 HTTP response code.


### Get_token_(Login)
#### Endpoint
- Type `POST`
- `/token/auth`
#### Inputs
```json
{
    email: "Email"
    password: "Password"
}
```
#### Returns

##### Valid
```json
{
    JWT_Token: "Token"
}
```
You can set it in the header of a request using the following snippets.
```java
//java
HttpRequest request = HttpRequest.newBuilder()
    .header("Authorization", "bearer " + token)
```
```javascript
//js with axios

axios.defaults.headers.common['Authorization'] = `bearer <token>`
```
```javascript
//js without axios
fetch('https://api.example.com/protected', {
  headers: {
    'Authorization': `bearer <token>`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));   
```
```javascript
const headers = new HttpHeaders({
  'Authorization': 'bearer my-token',
});

this.http.get('https://api.npms.io/v2/search?q=scope:angular', { headers }).subscribe(data => {
  this.totalAngularPackages = data.total;
});
```
##### Invalid

- 401 HTTP response code.

### Logout
#### Endpoint
- type `DELETE`
- `token/logout`
#### Inputs
- This endpoint takes the [JWT](https://datatracker.ietf.org/doc/html/rfc7515#section-3.3) token from the header and invalidates it.
#### Returns
##### Valid
- 200 HTTP response code.
- After this you must redirect the user to the login procedure.
##### Invalid
- 400 HTTP response code.


### Token_Validation 
#### Endpoint
- Type `GET`
- `/token/validate`
#### Inputs
This endpoint takes the [JWT](https://datatracker.ietf.org/doc/html/rfc7515#section-3.3) token from the header. 
#### Returns
##### Valid
- 200 HTTP response code.

##### Invalid
- 401 HTTP response code.

### Group_Check 
#### Endpoint
- Type `GET`
- `/token/groups`
#### Inputs
- This endpoint takes the [JWT](https://datatracker.ietf.org/doc/html/rfc7515#section-3.3) token from the header.

- The query string is optional and if it is present it will check if the user is in the specified groups. 

#### Returns

##### Valid
With query string.
```json
{
    isInGroups: true //or false
}
```

Without
```json
{
    Groups:["Group1",...,"GroupN"]
}
```

##### Invalid
- If the token is invalid it will return a 401 response code.


- If one of the groups does not exist it will return a 400 response code.


### Get email from token
#### Endpoint
- type `POST`
- `/token/email`
#### Inputs
- This endpoint takes the [JWT](https://datatracker.ietf.org/doc/html/rfc7515#section-3.3) token from the header.
#### Returns
##### Valid
- 200 HTTP response code

##### Invalid
- 400 HTTP response code


### Request_password_recovery
#### Endpoint
- Type `POST`
- `/recovery`
#### Inputs
```json
{
    "email":"email"
    "recepient":"email | number | ...",
    "channel":"email | sms | ..."
}
```
#### Returns
##### Valid

- When called this endpoint will send an email to the user email address.
##### Invalid
- A 400 HTTP response code if the IP has been rate limited.
- A 200 HTTP response code could mean that the call was successful or that the email was invalid (We will not reveal it with an error code to keep the information hidden from bad actors).
- A 501 HTTP response if the specified channel is not implemented

### Verify_password_recovery
#### Endpoint
- type `POST`
- `/recovery/auth`
#### Inputs
```json
{
    email: "email"
    otp: "code"
}
```
#### Returns
##### Valid
```json
{
    token: "Token"
}
```
This token cannot be used as a normal authentication token. It only grants the permission to change the password and lasts for about 1H. It will be invalidated after the password change.
##### Invalid
- 401 HTTP response code


### Change_password
#### Endpoint
- type `POST`
- `recovery/change-password`
#### Inputs
```json
{
    newPassword:"newPassword"
    token:"token"
}
```

#### Returns
##### Valid
- 200 HTTP response code.
- After this you must redirect the user to the login procedure.
##### Invalid
- 401 HTTP response code.