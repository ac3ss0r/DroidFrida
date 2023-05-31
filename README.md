<center><img width=100% src="https://github.com/acess0r/DroidFrida/blob/main/logo.png?raw=true"/></center>

DroidFrida is an android app for executing frida scripts directly on your android device. Frida is a dynamic instrumentation framework that allows to edit memory and hook methods, which is very useful when analyzing and cracking application (including games). Frida gives large posibilities, and I use it very often, so I created an app to use it easily.

<div width=100% align=center>
  <img width=30% src="https://github.com/ac3ss0r/DroidFrida/blob/main/preview1.png?raw=true"/>
  <img width=30% src="https://github.com/ac3ss0r/DroidFrida/blob/main/preview2.png?raw=true"/>
  <img width=30% src="https://github.com/ac3ss0r/DroidFrida/blob/main/preview3.png?raw=true"/>
</div>

### Progress

- [X] Basic frida script execution on the target app
- [X] Improve UI, add settings
- [X] Add example scripts list
- [ ] Implement code formatting and obfuscation

### Usage

You will need root access (or vphone) on your phone otherwise this won't work. Enter the package name of the target application as the -f flag and input your script  in the text field. You will be able to run target app with the frida injection.

### Documentation

You can find documentation for frida javascript api <a href="https://frida.re/docs/javascript-api/">here</a>.

