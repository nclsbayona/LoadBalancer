Para el sistema se decidió usar tres diferentes lenguajes de programación, esto no solo por las facilidades que brinda cada uno, sino porque de esta manera se vuelve más dificil vulnerar la seguridad de aplicación en tanto se busca llegar a una diferenciación de los entornos usados haciendo que sea más complicado tomar el control de todos a la vez. Los tres lenguajes de programación escogidos fueron Python (Para los clientes usados), Java (Para el balanceador de cargas) y Go (Para los servidores), adicionalmente, se implementó la base de datos con PostgreSQL de manera que los servidores en Go procesan las peticiones de los clientes y solicitan lo que sea que necesiten a la base de datos.

Es importante también mencionar que para facilidad al momento de poner en funcionamiento la aplicación, puesto que para poner en funcionamiento la misma es necesario configurar varias dependencias para permitir no solo que exista un proceso de seguridad en torno a las máquinas autorizadas para hacer parte de la aplicación, sino también para el uso de la librería ZMQ en si en el lenguaje Go, se decidió que se usaría docker como tecnología de contenerización la cual se usaría para levantar los contenedores referentes a los diferentes componentes de la aplicación, en diferentes máquinas, pero evitando que se lleve a cabo un aislamiento de la pila de red, esto buscando que los procesos de los diferentes componentes de la aplicación tengan la apariencia de ejecutarse como procesos de la máquina en si y no de un contenedor.


En el directorio client se encuentran dos archivos de código fuente escritos en lenguaje Python, así como un archivo de texto que contiene las dependencias necesarias para ejecutar estos archivos: 
- El archivo client.py contiene el código para ejecutar un cliente del balanceador de cargas. Este cliente se conectará al balanceador y una vez hecho esto pedirá al usuario que ingrese la petición a enviar.
- El archivo client.py contiene el código para ejecutar un cliente del balanceador de cargas. Este archivo a diferencia del client.py no pide al usuario ninguna entrada y simplemente enviará una petición de un conjunto de peticiones previamente establecidas.
- El archivo requirements.txt contiene las dependencias necesarias con su respectiva versión utilizada. Para instalar estas, en un entorno que tenga python versión 3.X y su respectivo manejador de dependencias pip es necesario ejecutar 'pip install -r requirements.txt'.

En el directorio load-balancer se encuentran los archivos necesarios para que funcione el balanceador de cargas, hay principalmente dos grandes grupos de tanto archivos como directorios en este directorio:
- Por un lado tenemos los archivos relacionados con el servidor auxiliar que inicia la aplicación una vez que se detecta que se necesita del mismo, en este grupo tenemos 'server.go', 'go.mod', 'go.sum' y 'server'.
- Por otro lado tenemos los archivos y directorios relacionados con el balanceador de cargas en si. Es necesario decir que aquí decidió usarse maven como administrador de paquetes para Java en tanto el mismo permite declarar las dependencias necesarias, así como sus versiones y este se encarga de descargar esta y asegurarse este disponible para la aplicación cuando esta lo requiera.

En el directorio web-server hay dos sub-directorios: 'Server' y 'Database':
- En el sub-directorio 'Server' hay a su vez otro sub-directorio 'src' el cual contiene los archivos necesarios para la puesta en funcionamiento del servidor. El contenido de estos archivos es similar al de aquellos que encontramos en el directorio 'load-balancer' explicado anteriormente, no obstante, hay una diferencia en tanto las direcciones IP a las que se accede son diferentes, tanto para el acceso a la base de datos como al balanceador en si puesto que la idea es que el balanceador de cargas se ejecute en una máquina diferente a la de los servidores (Salvo en el caso del servidor auxiliar).
- En el sub-directoriop 'Database' hay dos archivos: 'compose.yml' y 'init.sql', la idea es levantar un contenedor que permita exponer el servicio de base de datos haciendo uso de PostgreSQL, el cual debe iniciar partiendo de los parámetros específicados en 'init.sql'.


Referente a las dependencias necesarias para poner en funcionamiento la aplicación, debemos decir que se necesita de las dependencias.
Nota: Los nombres de las dependencias pueden variar dependiendo del sistema operativo a usar. En este caso, las dependencias se muestran como se encontrarian en un sistema operativo GNU/Linux, específicamente una distribución Ubuntu 22.04.
golang
libtool 
pkg-config 
build-essential 
autoconf 
automake 
libzmq3-dev 
python3 
python3-pip 
libczmq-dev
maven

Además de esto es necesario que se descargue la libreria libsodium para que se ejecuten algunos de los procesos de seguridad (Al menos en Go), los pasos a continuación presentan los llevados a cabo para instalar la librería en un sistema operativo GNU/Linux, específicamente una distribución Ubuntu 22.04. En caso de manejarse otro sistema operativo es necesario leer la documentación de libsodium e instalar la libreria siguiendo las instrucciones oficiales (https://doc.libsodium.org/installation) :
Descargar el paquete de libsodium desde: https://download.libsodium.org/libsodium/releases/libsodium-1.0.17-stable.tar.gz
Descomprimir el paquete libsodium-1.0.17-stable.tar.gz
Cambiar al directorio 'libsodium-1.0.17-stable'
Ejecutar el archivo 'configure'
Ejecutar en el directorio los comandos 'make', 'make check' y 'make install'

Para el correcto funcionamiento de la aplicación (Al menos en Go), los pasos a continuación presentan los llevados a cabo para instalar la librería en un sistema operativo GNU/Linux, específicamente una distribución Ubuntu 22.04. En caso de manejarse otro sistema operativo es necesario leer la documentación de libsodium e instalar la libreria siguiendo las instrucciones oficiales (http://download.zeromq.org)
Descargar el paquete de ZeroMQ desde: http://download.zeromq.org/zeromq-4.1.4.tar.gz
Descomprimir el archivo zeromq-4.1.4.tar.gz
Ejecutar el archivo 'autogen.sh' y el archivo 'configure'
RUN make install
RUN ldconfig

Para ejecutar los archivos de los clientes
Ejecutar el comando 'pip install -r requirements.txt'

No obstante y como se mencionó en un momento anterior, se creó una imagen de Docker para abstraer de cierto modo la complejidad de llevar a cabo la configuración de los componentes, la misma está disponible en Docker Hub bajo el nombre 'nclsbayona/zeromq-dev', el archivo bajo el cual la imagen fue creada corresponde a 'Dockerfile' que se encuentra en el directorio actual. Es decir que para evitar la complejidad de esta configuración, puede hacerse uso de esta imagen para levantar los contenedores y ya,
