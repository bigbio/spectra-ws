Search by peptide sequence
===================================

Swagger
-------
We recommend not to use browser for this as the amount of data could be really huge

.. warning::
   'peptideSequenceRegex' parameter should contain at-least 4 valid characters

   valid: AS*DF, ASDF* etc.,

   invalid: AS*F, ASF* etc.,


Curl
-----
.. code-block:: bash

   curl -X GET "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/sse/findByPepSequence?peptideSequenceRegex=AVC*KR" -H "accept: */*"

   OR

   curl -X GET "https://www.ebi.ac.uk/pride/multiomics/ws/spectra/stream/findByPepSequence?peptideSequenceRegex=AVC*KR" -H "accept: */*"



Python sample code
------------------

Using SSEs
***********

.. note::
   pip install sseclient-py

.. code-block:: python

   from sseclient import SSEClient   #pip install sseclient-py
   import requests

   url = 'https://www.ebi.ac.uk/pride/multiomics/ws/spectra/sse/findByPepSequence?peptideSequenceRegex=AVC*KR'

   def main():
       response = requests.get(url, stream=True)
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

   url = 'https://www.ebi.ac.uk/pride/multiomics/ws/spectra/stream/findByPepSequence?peptideSequenceRegex=AVC*KR'

   def main1():
       response = requests.get(url, stream=True)
       if response.status_code != 200:
           text = str(response.status_code) + ': ' + response.text
           raise Exception(text)
       for line in response.iter_lines():
           if line:
               print(line)


   if __name__ == "__main__":
       main1()

