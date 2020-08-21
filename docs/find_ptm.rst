Get Spectra using Ptm & Peptide Sequence
==========================================

Swagger
-------
We recommend not to use browser for this as the amount of data could be really huge

Sample request payloads
------------------------

sample1
.. code-block:: JSON

    {
        "peptideSequenceRegex": "AQLG*",
        "positions": [9, 16],
        "ptmKey": "name",
        "ptmValue": "iTRAQ4plex"

    }

sample2
.. code-block:: JSON

   {
    "peptideSequenceRegex": "AQLG*",
    "positions": [9, 16],
    "ptmKey": "accession",
    "ptmValue": "UNIMOD:214"

   }

sample3
.. code-block:: JSON

    {
        "peptideSequenceRegex": "AQLG*",
        "positions": [9, 16],
        "ptmKey": "mass",
        "ptmValue": "144.102063"

    }

.. warning::
   'peptideSequenceRegex' parameter should contain at-least 4 valid characters

   valid: AS*DF, ASDF* etc.,

   invalid: AS*F, ASF* etc.,

.. warning::
   'ptmKey' should be one of these: 'name, accession, mass' and 'ptmValue' should be it's corresponding value

Curl
-----
.. code-block:: bash

   curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/sse/findByPtm" -H "accept: */*" -H "Content-Type: application/json" -d '{"peptideSequenceRegex":"AQLG*","positions":[9,16],"ptmKey":"mass","ptmValue":"144.102063"}'

   OR

   curl -X POST "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/stream/findByPtm" -H "accept: */*" -H "Content-Type: application/json" -d '{"peptideSequenceRegex":"AQLG*","positions":[9,16],"ptmKey":"mass","ptmValue":"144.102063"}'


Python sample code
------------------

Using SSEs
***********

.. note::
   pip install sseclient-py

.. code-block:: python

   from sseclient import SSEClient   #pip install sseclient-py
   import requests

   url = 'https://www.ebi.ac.uk/pride/multiomics/ws/spectra/sse/findByPtm'
   headers = {"Content-Type": "application/json"}
   data = '{"peptideSequenceRegex":"AQLG*","positions":[9,16],"ptmKey":"mass","ptmValue":"144.102063"}'

   def main():
       response = requests.post(url, data=data, headers=headers, stream=True)
       if response.status_code != 200:
           text = str(response.status_code) + ': ' + response.text
           raise Exception(text)
       client = SSEClient(response)
       for event in client.events():
           if event.event.lower() == "spectrum":
               print(event.data)
           elif event.event.lower() == "done":
               client.close()
               break


   if __name__ == "__main__":
       main()

Using Stream
*************

.. code-block:: python

   import requests

   url = 'https://www.ebi.ac.uk/pride/multiomics/ws/spectra/stream/findByPtm'
   headers = {"Content-Type": "application/json"}
   data = '{"peptideSequenceRegex":"AQLG*","positions":[9,16],"ptmKey":"mass","ptmValue":"144.102063"}'

   def main1():
       response = requests.post(url, data=data, headers=headers, stream=True)
       if response.status_code != 200:
           text = str(response.status_code) + ': ' + response.text
           raise Exception(text)
       for line in response.iter_lines():
           if line:
               print(line)


   if __name__ == "__main__":
       main1()

