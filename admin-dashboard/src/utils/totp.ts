/**
 * totp.ts — Self-contained TOTP (RFC 6238) utility using Web Crypto API.
 * No external TOTP library required for browser-side verification.
 */

// ---------- Base32 Decode ----------
const BASE32_CHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567';

function base32Decode(input: string): Uint8Array {
  const str = input.replace(/=+$/, '').toUpperCase();
  let bits = 0;
  let value = 0;
  const output: number[] = [];

  for (const ch of str) {
    const idx = BASE32_CHARS.indexOf(ch);
    if (idx === -1) continue;
    value = (value << 5) | idx;
    bits += 5;
    if (bits >= 8) {
      output.push((value >> (bits - 8)) & 0xff);
      bits -= 8;
    }
  }
  return new Uint8Array(output);
}

// ---------- HMAC-SHA1 ----------
async function hmacSha1(key: Uint8Array, data: Uint8Array): Promise<Uint8Array> {
  const keyBuf = key.buffer.slice(key.byteOffset, key.byteOffset + key.byteLength) as ArrayBuffer;
  const dataBuf = data.buffer.slice(data.byteOffset, data.byteOffset + data.byteLength) as ArrayBuffer;
  const cryptoKey = await crypto.subtle.importKey(
    'raw', keyBuf, { name: 'HMAC', hash: 'SHA-1' }, false, ['sign']
  );
  const sig = await crypto.subtle.sign('HMAC', cryptoKey, dataBuf);
  return new Uint8Array(sig);
}

// ---------- TOTP core ----------
async function computeTotp(secret: string, timeStep?: number): Promise<string> {
  const step = timeStep ?? Math.floor(Date.now() / 1000 / 30);
  const stepBytes = new Uint8Array(8);
  // Write step as big-endian 64-bit
  let tmp = step;
  for (let i = 7; i >= 0; i--) {
    stepBytes[i] = tmp & 0xff;
    tmp = Math.floor(tmp / 256);
  }
  const keyBytes = base32Decode(secret);
  const hash = await hmacSha1(keyBytes, stepBytes);

  const offset = hash[hash.length - 1] & 0x0f;
  const code =
    ((hash[offset] & 0x7f) << 24) |
    ((hash[offset + 1] & 0xff) << 16) |
    ((hash[offset + 2] & 0xff) << 8) |
    (hash[offset + 3] & 0xff);

  return String(code % 1_000_000).padStart(6, '0');
}

/** Verify a 6-digit TOTP code. Accepts current window ±1 step for clock drift. */
export async function verifyTotp(token: string, secret: string): Promise<boolean> {
  const now = Math.floor(Date.now() / 1000 / 30);
  for (const delta of [-1, 0, 1]) {
    const expected = await computeTotp(secret, now + delta);
    if (expected === token) return true;
  }
  return false;
}

/** Generate a cryptographically random Base32 secret (160 bits). */
export function generateTotpSecret(): string {
  const bytes = crypto.getRandomValues(new Uint8Array(20));
  let result = '';
  let buffer = 0;
  let bitsLeft = 0;
  for (const byte of bytes) {
    buffer = (buffer << 8) | byte;
    bitsLeft += 8;
    while (bitsLeft >= 5) {
      result += BASE32_CHARS[(buffer >> (bitsLeft - 5)) & 31];
      bitsLeft -= 5;
    }
  }
  if (bitsLeft > 0) result += BASE32_CHARS[(buffer << (5 - bitsLeft)) & 31];
  return result;
}

/** Build the otpauth:// provisioning URI for a QR code. */
export function buildOtpAuthUri(email: string, secret: string): string {
  const label = encodeURIComponent(`FlipWise Admin:${email}`);
  const issuer = encodeURIComponent('FlipWise Admin');
  return `otpauth://totp/${label}?secret=${secret}&issuer=${issuer}&algorithm=SHA1&digits=6&period=30`;
}
