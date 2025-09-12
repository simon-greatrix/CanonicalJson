const fs = require('fs')

const outputFile = "../../resources/ijson_serial_numbers.txt"

const serialU64s = new BigUint64Array(2000)
for (i = 0; i < 2000; i++) {
	serialU64s[i] = 0x0010000000000000n + BigInt(i)
}
const serialF64s = new Float64Array(serialU64s.buffer)

const f = fs.createWriteStream(outputFile)
for (i = 0; i < serialU64s.length; i++) {
  const u64 = serialU64s[i];
  const f64 = serialF64s[i];
  line = BigInt.asIntN(64,u64) + "," + f64.toString() + "\n"
  f.write(line)
}
f.close()

