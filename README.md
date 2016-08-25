
# Eclipse Plugin

<a href="http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=2815718" class="drag" title="Drag to your running Eclipse workspace to install monalisa-eclipse"><img class="img-responsive" src="https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png" alt="Drag to your running Eclipse workspace to install monalisa-orm" /></a>

OR
* Download [plugin](https://github.com/11039850/monalisa-orm/raw/master/plugins/com.tsc9526.monalisa.plugin.eclipse_1.7.0.jar) ,Place the jar to : eclipse/plugins
* Restart Eclipse

## Multiline Strings 

Plugin installed, you can write multiline strings. For detail: [Multiline Syntax](https://github.com/11039850/monalisa-orm/wiki/Multiple-line%20syntax)

Effect as shown below:

![image](https://github.com/11039850/monalisa-orm/raw/master/doc/images/multi_lines.png)


## Auto generate the model classes (Saved: Ctrl+S）

* Need to set up project(Properties->Java Compiler)
 
  JDK compliance: **>= 1.6** 
* Need to set up project(Properties->Java Compiler->Annotation Processing)

  Enable this option: **Enable annotation processing**

Effect as shown below:

![image](https://github.com/11039850/monalisa-orm/raw/master/doc/images/db_save.jpg)

## Auto generate DTOs according the SQL code
* Need to set up eclipse (Window->Preferences->Java->Editor->Save Action -> Configure -> monalisa)

  Enable this option: **@Select** 

Effect as shown below:

![image](https://github.com/11039850/monalisa-db/raw/master/doc/images/select_save.jpg)