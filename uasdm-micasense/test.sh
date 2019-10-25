docker run --mount type=bind,src=$(pwd),dst=/opt/micawork -e MICASENSE_OUT=/opt/micawork/out -e MICASENSE_IN=/opt/micawork/in uasdm-micasense
