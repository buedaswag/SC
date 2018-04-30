How were the keystores generated? 
 PUBLIC AND PRIVATE KEY PAIR : keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -storetype Jceks -keystore myKeys.keystore
 SECRET KEY FOR MANUSERS :  -genseckey -alias seckey -storetype jceks -keystore myKeys.keystore

CLIENTE

// Importar certificado do servidor
keytool -importcert -alias server -file server.cer -keystore client.keystore

repository: this folder contains the images that are used to demonstrate this project. 

Sandbox configuration for JVM (server):

-Djava.security.manager -Djava.security.policy==server.policy

Sandbox configuration for JVM (client):

-Djava.security.manager -Djava.security.policy=client.policy

Run configurations for Use Case testing:

Servidor:
UC1 -a
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -a ferias.webp,casa.JPG
		$PhotoShare max passmax 127.0.0.1:23232 -a gato.jpg,cao.jpg
		$PhotoShare antonio passantonio 127.0.0.1:23232 -a bug.jpg,movie.png		
UC2 -l
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -l antonio
		$PhotoShare max passmax 127.0.0.1:23232 -l miguel
		$PhotoShare antonio passantonio 127.0.0.1:23232 -l max
	Error function - User joaquina doesnt exist
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -l joaquina	
UC3 -g
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -g antonio
		$PhotoShare max passmax 127.0.0.1:23232 -g miguel
		$PhotoShare antonio passantonio 127.0.0.1:23232 -g max
	Error function - antonio is not a follower of qwerty
		$PhotoShare antonio passantonio 127.0.0.1:23232 -g qwerty

UC4 -L
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -L max cao.jpg
		$PhotoShare max passmax 127.0.0.1:23232 -L miguel ferias.webp
		$PhotoShare antonio passantonio 127.0.0.1:23232 -L max cao.jpg
	Error function - User antonio doesnt exist
		$PhotoShare fernando passfernando 127.0.0.1:23232 -L antonio movie.png
UC5 -D
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -D antonio bug.jpg
		$PhotoShare max passmax 127.0.0.1:23232 -D antonio bug.jpg
		$PhotoShare antonio passantonio 127.0.0.1:23232 -D max cao.jpg
	Error function - dislike in a photo that doesnt exist
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -D antonio naoExiste.jpg
		
UC6 -c
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -c ola! antonio movie.png
		$PhotoShare max passmax 127.0.0.1:23232 -c haha antonio movie.png
		$PhotoShare antonio passantonio 127.0.0.1:23232 -c "que foto de treta..." max cao.jpg
	Error function - User antonio doesnt exist
		$PhotoShare fernando passfernando 127.0.0.1:23232 -c "ola, eu sou o fernando!" antonio movie.png				
UC7 -f
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -f max,antonio
		$PhotoShare max passmax 127.0.0.1:23232 -f antonio,miguel
		$PhotoShare antonio passantonio 127.0.0.1:23232 -f max,miguel
	Error function - max and miguel are already followers of antonio
		$PhotoShare antonio passantonio 127.0.0.1:23232 -f max,miguel
	
UC8 -r
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -r max,antonio
		$PhotoShare max passmax 127.0.0.1:23232 -r miguel,antonio
		$PhotoShare antonio passantonio 127.0.0.1:23232 -r max
	Error function - fernando is not a follower of antonio (Se algum dos utilizadores não fizer parte da lista de seguidores deve ser devolvido um erro.)
		$PhotoShare antonio passantonio 127.0.0.1:23232 -r fernando
UC9 -i 
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 -i antonio bug.jpg
		$PhotoShare max passmax 127.0.0.1:23232 -i miguel casa.JPG
		$PhotoShare antonio passantonio 127.0.0.1:23232 -i antonio bug.jpg
	Error function - user lol doesnt exist
		$PhotoShare antonio passantonio 127.0.0.1:23232 -i lol nada.jpg

ManUsers
	Arguments:
		$ManUsers port keystore_alias keystore_password mac_password
		$ManUsers 23232 seckey 123456 123456
UC1 addUser
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 addUser
UC2 removeUser
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 removeUser
UC2 updatePassword
	Normal function
		$PhotoShare miguel passmiguel 127.0.0.1:23232 updatePassword newpassmiguel		