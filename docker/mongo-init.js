use admin
db.createUser(
   {
     user: "root",
     pwd: "secure",
     roles: [ 
       { role: "userAdminAnyDatabase", db: "admin" },
       { role: "readWriteAnyDatabase", db: "admin" } 
     ]
   }
 );