const handleErrors = (response) => {
    if (!response.ok) {
        throw Error(response.statusText);
    }
    return response;
}

const fileUpload = (
    file,
    fileUploadEndpoint,
    onSuccess,
    onFailure
) => {
    const formData = new FormData()
    formData.append('file', file)

    fetch(fileUploadEndpoint, {
        method: 'POST',
        body: formData
    })
        .then(handleErrors)
        .then((response) => response.json())
        .then(onSuccess)
        .catch(onFailure)
}