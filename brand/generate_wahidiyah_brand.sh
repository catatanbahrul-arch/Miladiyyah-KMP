#!/bin/bash
# WAHIDIYAH BRAND GENERATOR
# Script ini menghasilkan SVG logo original Wahidiyah yang terinspirasi dari
# nilai spiritual, ketenangan, dan harmoni (W shape + kubah/daun abstrak).
# Warna: Deep Green & Lime.

cat << 'SVG_EOF' > brand/logo/wahidiyah_mark.svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
  <path d="M20,30 L40,80 L50,55 L60,80 L80,30 L68,30 L60,60 L50,35 L40,60 L32,30 Z" fill="#1C5B2D"/>
  <path d="M50,15 Q65,35 60,60 Q50,45 50,15 Z" fill="#73C34F"/>
</svg>
SVG_EOF
echo "Logo SVG Wahidiyah Original berhasil di-generate di brand/logo/"
