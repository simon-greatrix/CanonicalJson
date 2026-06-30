const crypto = require("crypto");
const fs = require('fs');
const zlib = require("zlib");
const {Readable} = require("stream");
const {pipeline} = require("stream/promises");
const {tmpdir} = require('node:os');
const {join} = require('node:path');

// We write to a temporary file rather than dropping a huge file into our working environment.
const tempRoot = tmpdir();
const outputPath = join(tempRoot, 'ijson_random_numbers.txt.gz');

// Number of samples to generate
const numLines = 100000000;


const MASK64 = ((1n << 64n) - 1n);

// rotate-left for 64-bit BigInt
function rol64(x, k) {
  return ((x << BigInt(k)) | (x >> BigInt(64 - k))) & MASK64;
}

// Random number generator.
class Xoshiro256pp {
  constructor(seed) {
    if (!seed || seed.length !== 4) {
      throw new Error("Seed must be an array of 4 BigInts");
    }
    this.jumpCount = 0;
    this.state = seed.slice();
  }


  next() {
    const state = this.state;
    this.jumpCount--;

    if (this.jumpCount < 0) {
      // Do jump with SHA-256
      this.jumpCount = 1000;
      const ab = new ArrayBuffer(32);
      const dvIn = new DataView(ab);
      for (let i = 0; i < 4; i++) {
        dvIn.setBigUint64(i * 8, state[i]);
      }

      const buf = crypto.createHash('sha256').update(Buffer.from(new Uint8Array(ab))).digest();

      const dvOut = new DataView(buf.buffer, buf.byteOffset, buf.byteLength);
      for (let i = 0; i < 4; i++) {
        state[i] = dvOut.getBigUint64(i * 8);
      }
    }

    const result = (rol64((state[0] + state[3]) & MASK64, 23) + state[0]) & MASK64;
    const t = (state[1] << 17n) & MASK64;

    state[2] ^= state[0];
    state[3] ^= state[1];
    state[1] ^= state[2];
    state[0] ^= state[3];

    state[2] ^= t;
    state[3] = rol64(state[3], 45);

    return result; // ensure 64-bit result
  }
}

const rng = new Xoshiro256pp([1n, 2n, 3n, 4n]);


const u64 = new BigUint64Array(1)
const f64 = new Float64Array(u64.buffer)

const source = Readable.from((function* () {
  for (let i = 0; i < numLines; i++) {
    do {
      u64[0] = rng.next();
    } while (!isFinite(f64[0]));

    if (i % 100000 === 0) {
      console.log("Wrote " + i + " lines - "+Math.round(100*i/numLines)+"%");
    }

    line = f64[0].toString() + "\n";
    yield line;
  }
})(), {encoding: "utf8"});

const gzip = zlib.createGzip(
    {
      level: zlib.constants.Z_BEST_COMPRESSION
    }
);

(async () => {
  await pipeline(
      source,
      gzip,
      fs.createWriteStream(outputPath)
  );
})().catch(err => {
  console.error('pipeline failed:', err);
});

const f = fs.createWriteStream("../../resources/ijson_random_numbers.txt");
f.write(outputPath);
f.end();

