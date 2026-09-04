/**
 * Rasterises design/brand/app-icon.svg into the legacy launcher bitmaps.
 *
 *   node tools/build_launcher.js
 *
 * Adaptive launchers use the vector drawables in res/drawable; these WebP
 * files are only the pre-API-26 fallback, so the 108pt master is cropped to
 * its 72pt visible area and masked (squircle for ic_launcher, circle for
 * ic_launcher_round) the same way the platform would.
 */
const fs = require("fs");
const path = require("path");
const sharp = require("/tmp/svgtools/node_modules/sharp");

const ROOT = path.resolve(__dirname, "..");
const MASTER = path.join(ROOT, "design/brand/app-icon.svg");
const DENSITIES = {
  mdpi: 48,
  hdpi: 72,
  xhdpi: 96,
  xxhdpi: 144,
  xxxhdpi: 192,
};

// the adaptive icon's visible area is the middle 72 of the 108pt canvas
const VISIBLE = 72 / 108;

function squircle(size) {
  const r = size * 0.225;
  return Buffer.from(
    `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}">` +
      `<rect width="${size}" height="${size}" rx="${r}" ry="${r}" fill="#fff"/></svg>`
  );
}

function circle(size) {
  const r = size / 2;
  return Buffer.from(
    `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}">` +
      `<circle cx="${r}" cy="${r}" r="${r}" fill="#fff"/></svg>`
  );
}

async function render(size, maskSvg, out) {
  const full = Math.round(size / VISIBLE);
  const inset = Math.round((full - size) / 2);
  const base = await sharp(fs.readFileSync(MASTER), { density: 900 })
    .resize(full, full)
    .extract({ left: inset, top: inset, width: size, height: size })
    .png()
    .toBuffer();
  const mask = await sharp(maskSvg).resize(size, size).png().toBuffer();
  await sharp(base)
    .composite([{ input: mask, blend: "dest-in" }])
    .webp({ quality: 92, alphaQuality: 100 })
    .toFile(out);
  console.log("wrote", path.relative(ROOT, out));
}

(async () => {
  for (const [density, size] of Object.entries(DENSITIES)) {
    const dir = path.join(ROOT, "app/src/main/res", `mipmap-${density}`);
    fs.mkdirSync(dir, { recursive: true });
    await render(size, squircle(size), path.join(dir, "ic_launcher.webp"));
    await render(size, circle(size), path.join(dir, "ic_launcher_round.webp"));
  }
  // store / marketing render
  const play = path.join(ROOT, "design/brand/app-icon-512.png");
  await sharp(fs.readFileSync(MASTER), { density: 1600 }).resize(512, 512).png().toFile(play);
  console.log("wrote", path.relative(ROOT, play));
})();
