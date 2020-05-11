====
    Copyright 2020 The Department of Interior

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

https://dzone.com/articles/create-an-angular-2-component-library-and-consume

I tried to create an angular module, but gave up when adding a geoprism dependency like so:

"geoprism": "file:../ng2-lib"

caused this error:
Please add a @Pipe/@Directive/@Component annotation 

My conclusion is that for some reason the way the dependency is defined it isn't loading the source properly



Then, I copied the typescript source over from geoprism and upgraded a bunch of it (because it was throwing compile errors because uasdm is using a newer version of typescript).
I got the code the compile, however unfortunately when I run it in a web browser it never runs the main.ts file so I essentially have run out of time and have to give up on this.