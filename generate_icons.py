import subprocess

RESOLUTIONS = {
    'ldpi' : 36,
    'mdpi' : 48,
    'hdpi' : 72,
    'xhdpi' : 96
}

FILES = {
    'ntasks.svg' : 'ic_launcher.png',
    'ntasks-light.svg' : 'ic_launcher_light.png'
}

for infile, outfile in FILES.items():
    for folder, size in RESOLUTIONS.items():
        subprocess.call([
            'convert', 
            '-geometry',
            '%dx%d' % (size, size),
            '-background',
            'transparent',
            infile,
            'Android/res/drawable-%s/%s' % (folder, outfile)
        ])