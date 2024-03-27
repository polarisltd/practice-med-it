use admin
db.createUser(
   {
     user: "root1",
     pwd: "secure",
     roles: [ 
       { role: "userAdminAnyDatabase", db: "admin" },
       { role: "readWriteAnyDatabase", db: "admin" } 
     ]
   }
 );