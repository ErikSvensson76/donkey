### Basic Docker instructions:

##### To build: 
``docker build -t {image name} .``

##### To run:
``docker run -d -p 8010:8010  -v {volume name}:/data --name {image name}``