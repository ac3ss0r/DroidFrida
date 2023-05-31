var module; // global

async function main() {
	console.log("Waiting for the module to load...");
	while ((module = Module.findBaseAddress("libil2cpp.so")) == null) {
		await sleep(200);
	}
    console.log("Module loaded at " + module);
	Interceptor.attach(ptr(module).add(0xdeadbeef), { // method address here
    onEnter: function (args) {
      // process arguments
    },
    onLeave: function (retval) {
      // process return value
    }
  });
}

function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

main()
