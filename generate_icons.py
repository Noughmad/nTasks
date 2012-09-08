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

CATEGORY_SIZES = {
    'ldpi' : 48,
    'mdpi' : 48,
    'hdpi' : 64,
    'xhdpi' : 128
}

CATEGORY_NAMES = {
    'office' : 'office',
    'development' : 'housework',
    'education' : 'school',
    'graphics' : 'hobby',
    'education-miscellaneous' : 'other'
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
        
for infile, outfile in CATEGORY_NAMES.items():
    for new, orig in CATEGORY_SIZES.items():
        newSize = RESOLUTIONS[new]
        subprocess.call([
            'convert',
            '-geometry',
            '%dx%d' % (newSize, newSize),
            '/usr/share/icons/oxygen/%dx%d/categories/applications-%s.png' % (orig, orig, infile),
            'Android/res/drawable-%s/ic_category_%s.png' % (new, outfile)
        ])