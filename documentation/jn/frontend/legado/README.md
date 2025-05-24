SmartAdmin on ReactJS!
======================


* smartadmin 1.8.2
* react 15.3
* webpack 2 
* react-router 3

* lazy-loading
* large redux handled application
* many redux examples 
* voice control
* optimized builds
* many many more   
     

development
-----------
To run the code in your development environment:

1. Download and unpack
2. Run `npm install`
3. Start the development server for seed project with `npm run dev`
4. Point your browser to [http://localhost:2200](http://localhost:2200)


tip 
---
quick way to speed up builds when testing 
exclude routes from build by commenting them in `src/app/app.js`

***********************************************

build
-----
production build
`npm run build`

deploy on gcloud
----------------
gcloud config set project [PROJECT_ID]
gcloud app deploy

resources
---------
- [live project demo](http://sarj-shockwave.rhcloud.com)
- [webpack 2 docs](https://webpack.js.org)

-----------------------------------------------



******************************************************************

Notas:

Para obter o poligono da cidade:
-------------------------------

https://nominatim.openstreetmap.org/search.php?q=guarulhos&polygon_geojson=1&viewbox=

Entrar em detalhes e verificar o numero OSM

Digitar o numero no endereço

http://polygons.openstreetmap.fr/index.py?id=298165

E escolher o GeoJSON para download

No GEOJson alterar os itens

GeometryCollection para Feature
geometries para geometry e remover o array
MultiPolygon para Polygon 
e em coordinates remover um nivel do array


API's Rest disponíveis no Swagger UI: 
-------------------------------------

Equipamento ->  https://nxnetapi-equipamento-dot-noxxonsat-nxnet-valid.appspot.com/swagger-ui.html#/
Linha       ->  https://nxnetapi-linha-dot-noxxonsat-nxnet-valid.appspot.com/swagger-ui.html
Módulo      ->  https://nxnetapi-modulo-dot-noxxonsat-nxnet-valid.appspot.com/swagger-ui.html
SIM Card    ->  https://nxnetapi-sim-card-dot-noxxonsat-nxnet-valid.appspot.com/swagger-ui.html#/
Usuário     ->  https://nxnetapi-usuarios-dot-noxxonsat-nxnet-valid.appspot.com/swagger-ui.html
Veículo     ->  https://nxnetapi-veiculo-dot-noxxonsat-nxnet-valid.appspot.com/swagger-ui.html