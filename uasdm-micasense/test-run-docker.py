
def main():
    import subprocess

    cmd = 'docker run --mount type=bind,src=/home/rich/dev/projects/uasdm/git/imageprocessing/data,dst=/opt/micawork -e MICASENSE_OUT=/opt/micawork/out -e MICASENSE_IN=/opt/micawork/ALTUM1SET/000 micasense-docker'
    subprocess.check_call(cmd, shell=True)


if __name__ == '__main__':
    main()
