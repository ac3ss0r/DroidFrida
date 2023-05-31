var module; // global

async function main() {
	console.log("Waiting for the module to load...");
	while ((module = Module.findBaseAddress("libil2cpp.so")) == null) {
		await sleep(200);
	}
	console.log("Module loaded at " + module);
	hexPatch(module, 0xdeadbeef, "C0035FD6");
}

function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

function Hex2Bytes(hex) {
	hex = hex.replaceAll(" ", "")
	let bytes = [];
	for (let c = 0; c < hex.length; c += 2) {
		bytes.push(parseInt(hex.substr(c, 2), 16));
	}
	return bytes;
}

function hexPatch(base, addr, hex) {
	let target = ptr(base).add(addr)
	let data = Hex2Bytes(hex)
	Memory.patchCode(target, data.length, function(vfn) {
		Memory.writeByteArray(target, data);
		console.log("Patched " + data.length + " bytes on " + target);
	})
}

main()
