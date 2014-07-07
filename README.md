Assets maven plugin
===================

This is the maven plugin for minifing and aggregatings the stylesheet and javascript files.
It can precompile LESS or Coffeescript sources.

Goals
-----

Goal                   |Description
-----------------------|-------------------------------
assets:aggregate       |Aggregate asset files according to the recipe file.

### assets:aggregate

Full name
:net.unit8.maven.plugins:assets-maven-plugin:0.2.0-SNAPSHOT:aggregate

Description
:Aggregate asset files according to the recipe file.

#### Optional parameters

Name             |Type   |Description
-----------------|-------|--------------------------------------
recipeFile       |String |Recipe file path
encoding         |String |charset of source files
workingDirectory |String |The base directory of working files
auto             |Boolean|Wherher auto re-aggregate When source files are changed,

Recipe file
-----------

The recipe file is written how to aggregate asset files.

    sourceDirectory: src/main/webapp
    targetDirectory: src/main/webapp
    precompilers: [ less, coffee ]
    rules:
      - target: js/all.js
        version: 1.7
        components:
          - js/*.js
          - js/*.coffee
      - target: css/all.css
        version: 1.7
        minify: true
        components:
          - css/a1.css
          - css/a2.css
          - css/a3.less

Name             |Description
-----------------|-------------------------------------------
sourceDirectory  |The base directory of components
targetDirectory  |The base directory of targets
precompilers     |If assets is needed to precompile, this precompilers works.
target           |This is aggregated file.
version          |If the version is set, the name of target file
minify           |If this is set true, minify the aggregated asset file.
components       |This is subject to aggregate.


License
-------

Copyright 2012 kawasima

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
