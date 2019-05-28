https://dzone.com/articles/create-an-angular-2-component-library-and-consume

I tried to create an angular module, but gave up when adding a geoprism dependency like so:

"geoprism": "file:../ng2-lib"

caused this error:
Please add a @Pipe/@Directive/@Component annotation 

My conclusion is that for some reason the way the dependency is defined it isn't loading the source properly



Then, I copied the typescript source over from geoprism and upgraded a bunch of it (because it was throwing compile errors because uasdm is using a newer version of typescript).
I got the code the compile, however unfortunately when I run it in a web browser it never runs the main.ts file so I essentially have run out of time and have to give up on this.