#!/usr/bin/python
import subprocess
import os
import os.path
import shutil
import sys


def main():
    if not os.path.isdir('.git'):
        print 'Should run from the top-level of the git repo.'
        sys.exit(1)

    for file in os.listdir('img_src'):
        if file.endswith('.svg'):
            svg_path = os.path.join('img_src', file)
            base = file[:-4]

            for size in ['1x', '2x', '3x']:
                suffix = '' if size == '1x' else '@' + size
                png_path = os.path.join('img', base + suffix + '.png')
                print 'Generating %s' % png_path
                subprocess.call(['svgexport', svg_path, png_path, size])
        elif file.endswith('.png'):
            src_path = os.path.join('img_src', file)
            shutil.copy(src_path, './img/')

    print 'Done!'

if __name__ == '__main__':
    main()
