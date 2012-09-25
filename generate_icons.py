import subprocess
import shutil

RESOLUTIONS = {
    'ldpi' : 36,
    'mdpi' : 48,
    'hdpi' : 72,
    'xhdpi' : 96
}

CATEGORY_LARGE_RESOLUTIONS = {
    'ldpi' : 48,
    'mdpi' : 64,
    'hdpi' : 96,
    'xhdpi' : 128
}

NOTIFICATION_RESOLUTIONS = {
    'ldpi': 18,
    'mdpi': 24,
    'hdpi' : 36,
    'xhdpi' : 48
}

FILES = {
    'ntasks.svg' : 'ic_launcher.png',
    'ntasks-light.svg' : 'ic_launcher_light.png'
}

DPI_VALUES = ['ldpi', 'mdpi', 'hdpi', 'xhdpi']

KDE_ICON_SIZES = [16, 22, 24, 32, 48, 64, 128, 256]

CATEGORY_NAMES = {
    'office' : 'office',
    'education' : 'school',
    'graphics' : 'hobby',
    'toys' : 'sport',
    'education-miscellaneous' : 'other',
}

def get_best_size(wanted, available):
    for a in available:
        if a >= wanted:
            return a
    return available[-1]

def get_best_kde_size(wanted):
    return get_best_size(wanted, KDE_ICON_SIZES)

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
        
infile = 'ntasks-notification.svg'
outfile = 'ic_notification.png'
for folder, size in NOTIFICATION_RESOLUTIONS.items():
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
    for folder in DPI_VALUES:
        newSize = RESOLUTIONS[folder]
        kdeSize = get_best_kde_size(newSize)
        subprocess.call([
            'convert',
            '-geometry',
            '%dx%d' % (newSize, newSize),
            '/usr/share/icons/oxygen/%dx%d/categories/applications-%s.png' % (kdeSize, kdeSize, infile),
            'Android/res/drawable-%s/ic_category_%s.png' % (folder, outfile)
        ])
        newSize = CATEGORY_LARGE_RESOLUTIONS[folder]
        kdeSize = get_best_kde_size(newSize)
        subprocess.call([
            'convert',
            '-geometry',
            '%dx%d' % (newSize, newSize),
            '/usr/share/icons/oxygen/%dx%d/categories/applications-%s.png' % (kdeSize, kdeSize, infile),
            'Android/res/drawable-%s/ic_category_large_%s.png' % (folder, outfile)
        ])
        
for res in DPI_VALUES:
    for icon in ['refresh', 'add']:
        args = (res, icon)
        shutil.copyfile('/opt/android-sdk/platforms/android-16/data/res/drawable-%s/ic_menu_%s.png' % args,
                        'Android/res/drawable-%s/ic_menu_%s.png' % args)
