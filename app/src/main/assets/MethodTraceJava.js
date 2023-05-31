Java.perform(function () {
  var targetClass = 'com.example.targetapp.TargetClass'; // Replace with the class you want to trace

  var TargetClass = Java.use(targetClass);
  var methods = TargetClass.class.getDeclaredMethods();

  methods.forEach(function (method) {
    var methodName = method.toString().replace(targetClass + '.', '');

    method.setAccessible(true);

    var overloadCount = TargetClass[methodName].overloads.length;

    for (var i = 0; i < overloadCount; i++) {
      (function () {
        var currentMethod = TargetClass[methodName].overloads[i];
        currentMethod.implementation = function () {
          console.log('[+] ' + targetClass + '.' + methodName + ' called');
          
          var args = Array.prototype.slice.call(arguments);
          args.forEach(function (arg, index) {
            console.log('    Arg[' + index + ']: ' + JSON.stringify(arg));
          });

          var result = currentMethod.apply(this, arguments);
          console.log('    Result: ' + JSON.stringify(result));
          return result;
        };
      })();
    }
  });
});
